package br.com.docflow.application.usecase;

import br.com.docflow.application.service.CheckSignatureStatus;
import br.com.docflow.application.service.ContractSignedTaskAutomationService;
import br.com.docflow.application.service.ContractPersistenceService;
import br.com.docflow.application.service.DownloadSignedContract;
import br.com.docflow.application.service.NotifyContractSigned;
import br.com.docflow.application.service.StoredContractPdf;
import br.com.docflow.application.service.StoreSignedContractPdf;
import br.com.docflow.domain.model.Contract;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SynchronizeContractSignatureUseCase {

    private final CheckSignatureStatus checkSignatureStatus;
    private final DownloadSignedContract downloadSignedContract;
    private final StoreSignedContractPdf storeSignedContractPdf;
    private final ContractPersistenceService contractPersistenceService;
    private final NotifyContractSigned notifyContractSigned;
    private final ContractSignedTaskAutomationService contractSignedTaskAutomationService;

    public Contract execute(Contract contract) throws Exception {
        if (!checkSignatureStatus.shouldSynchronize(contract)) {
            return contract;
        }

        if (!checkSignatureStatus.isSigned(contract)) {
            return contract;
        }

        byte[] signedPdf = downloadSignedContract.execute(contract);
        StoredContractPdf storedPdf = storeSignedContractPdf.execute(contract, signedPdf);
        Contract saved;
        try {
            saved = contractPersistenceService.markAsSigned(contract, storedPdf.pdfUrl());
        } catch (RuntimeException persistenceError) {
            storeSignedContractPdf.cleanupIfCreated(storedPdf);
            throw persistenceError;
        }

        notifyContractSigned.execute(saved);
        contractSignedTaskAutomationService.createTasksForSignedContract(saved);
        return saved;
    }
}
