package br.com.docflow.application.usecase;

import br.com.docflow.application.port.ContractFileStorage;
import br.com.docflow.application.service.ContractPersistenceService;
import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.AdditionalSigner;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.ContractStatus;
import br.com.docflow.domain.repository.ContractRepository;
import br.com.docflow.domain.service.SignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SendContractUseCase {

    private final ContractRepository contractRepository;
    private final SignatureService signatureService;
    private final ContractFileStorage fileStorage;
    private final SynchronizeContractSignatureUseCase synchronizeContractSignatureUseCase;
    private final ContractPersistenceService contractPersistenceService;

    public Contract execute(Long contractId) {
        return execute(contractId, List.of());
    }

    public Contract execute(Long contractId, List<AdditionalSigner> additionalSigners) {
        try {
            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new NotFoundException("CONTRACT_NOT_FOUND", "Contrato nao encontrado"));

            if (contract.getPdfUrl() == null || contract.getPdfUrl().isEmpty()) {
                throw new BusinessException("CONTRACT_PDF_MISSING", "PDF nao foi gerado para este contrato ainda");
            }

            if (ContractStatus.ASSINADO.equals(contract.getStatus())) {
                throw new BusinessException("CONTRACT_ALREADY_SIGNED", "Contrato ja esta assinado e nao pode ser reenviado");
            }

            contract = synchronizeContractSignatureUseCase.execute(contract);
            if (ContractStatus.ASSINADO.equals(contract.getStatus())) {
                return contract;
            }

            byte[] pdfBytes = fileStorage.loadPdf(contract.getPdfUrl());
            String autentiqueId = signatureService.sendForSignature(
                    contract,
                    pdfBytes,
                    additionalSigners == null ? List.of() : additionalSigners
            );

            return contractPersistenceService.markAsSent(contract, autentiqueId);
        } catch (BusinessException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("SIGNATURE_SEND_FAILED", "Falha ao enviar contrato para assinatura", e);
        }
    }
}
