package br.com.docflow.application.service;

import br.com.docflow.application.port.ContractFileStorage;
import br.com.docflow.domain.model.Contract;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreSignedContractPdf {

    private final ContractFileStorage fileStorage;

    public StoredContractPdf execute(Contract contract, byte[] signedPdf) throws Exception {
        if (contract.getPdfUrl() == null || contract.getPdfUrl().isBlank()) {
            String pdfUrl = fileStorage.savePdf(signedPdf, "signed_contract_" + contract.getId());
            return new StoredContractPdf(pdfUrl, true);
        }

        String pdfUrl = fileStorage.overwritePdf(contract.getPdfUrl(), signedPdf);
        return new StoredContractPdf(pdfUrl, false);
    }

    public void cleanupIfCreated(StoredContractPdf storedPdf) {
        if (storedPdf == null || !storedPdf.createdNewFile()) {
            return;
        }

        try {
            fileStorage.deletePdfIfExists(storedPdf.pdfUrl());
        } catch (Exception ignored) {
            // A falha de limpeza nao deve esconder o erro original de persistencia.
        }
    }
}
