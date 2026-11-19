package br.com.docflow.application.usecase;

import br.com.docflow.application.service.ContractPersistenceService;
import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.ContractStatus;
import br.com.docflow.domain.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewContractUseCase {

    private final ContractRepository contractRepository;
    private final SynchronizeContractSignatureUseCase synchronizeContractSignatureUseCase;
    private final ContractPersistenceService contractPersistenceService;

    public Contract execute(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("CONTRACT_NOT_FOUND", "Contrato nao encontrado"));

        if (contract.getStatus() == ContractStatus.ASSINADO) {
            throw new BusinessException("CONTRACT_ALREADY_SIGNED", "Contrato assinado nao pode ser revisado");
        }

        try {
            contract = synchronizeContractSignatureUseCase.execute(contract);
        } catch (Exception e) {
            throw new BusinessException("SIGNATURE_STATUS_CHECK_FAILED", "Nao foi possivel consultar o status do contrato no Autentique", e);
        }

        if (contract.getStatus() == ContractStatus.ASSINADO) {
            return contract;
        }

        return contractPersistenceService.markAsDraftForReview(contract);
    }
}
