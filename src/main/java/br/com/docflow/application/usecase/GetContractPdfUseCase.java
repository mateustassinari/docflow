package br.com.docflow.application.usecase;

import br.com.docflow.application.port.ContractFileStorage;
import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetContractPdfUseCase {

    private final ContractRepository contractRepository;
    private final ContractFileStorage fileStorage;

    public byte[] execute(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("CONTRACT_NOT_FOUND", "Contrato nao encontrado"));

        if (contract.getPdfUrl() == null || contract.getPdfUrl().isBlank()) {
            throw new BusinessException("CONTRACT_PDF_MISSING", "PDF nao foi gerado para este contrato ainda");
        }

        try {
            return fileStorage.loadPdf(contract.getPdfUrl());
        } catch (Exception e) {
            throw new BusinessException("CONTRACT_PDF_LOAD_FAILED", "Falha ao carregar o PDF do contrato", e);
        }
    }
}
