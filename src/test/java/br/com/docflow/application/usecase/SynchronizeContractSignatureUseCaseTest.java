package br.com.docflow.application.usecase;

import br.com.docflow.application.service.CheckSignatureStatus;
import br.com.docflow.application.service.ContractSignedTaskAutomationService;
import br.com.docflow.application.service.ContractPersistenceService;
import br.com.docflow.application.service.DownloadSignedContract;
import br.com.docflow.application.service.NotifyContractSigned;
import br.com.docflow.application.service.StoredContractPdf;
import br.com.docflow.application.service.StoreSignedContractPdf;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.ContractStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SynchronizeContractSignatureUseCaseTest {

    private final CheckSignatureStatus checkSignatureStatus = mock(CheckSignatureStatus.class);
    private final DownloadSignedContract downloadSignedContract = mock(DownloadSignedContract.class);
    private final StoreSignedContractPdf storeSignedContractPdf = mock(StoreSignedContractPdf.class);
    private final ContractPersistenceService contractPersistenceService = mock(ContractPersistenceService.class);
    private final NotifyContractSigned notifyContractSigned = mock(NotifyContractSigned.class);
    private final ContractSignedTaskAutomationService contractSignedTaskAutomationService = mock(ContractSignedTaskAutomationService.class);
    private final SynchronizeContractSignatureUseCase useCase = new SynchronizeContractSignatureUseCase(
            checkSignatureStatus,
            downloadSignedContract,
            storeSignedContractPdf,
            contractPersistenceService,
            notifyContractSigned,
            contractSignedTaskAutomationService
    );

    @Test
    void shouldSkipWhenContractShouldNotSynchronize() throws Exception {
        Contract contract = new Contract();

        when(checkSignatureStatus.shouldSynchronize(contract)).thenReturn(false);

        Contract result = useCase.execute(contract);

        assertThat(result).isSameAs(contract);
        verify(checkSignatureStatus, never()).isSigned(contract);
        verify(downloadSignedContract, never()).execute(contract);
    }

    @Test
    void shouldSkipWhenContractIsNotSigned() throws Exception {
        Contract contract = sentContract();

        when(checkSignatureStatus.shouldSynchronize(contract)).thenReturn(true);
        when(checkSignatureStatus.isSigned(contract)).thenReturn(false);

        Contract result = useCase.execute(contract);

        assertThat(result).isSameAs(contract);
        verify(downloadSignedContract, never()).execute(contract);
        verify(notifyContractSigned, never()).execute(contract);
    }

    @Test
    void shouldPersistAndNotifyWhenSigned() throws Exception {
        Contract contract = sentContract();
        Contract saved = Contract.restore(1L, null, null, ContractStatus.ASSINADO, "signed.pdf", "autentique-id");

        when(checkSignatureStatus.shouldSynchronize(contract)).thenReturn(true);
        when(checkSignatureStatus.isSigned(contract)).thenReturn(true);
        when(downloadSignedContract.execute(contract)).thenReturn("%PDF signed".getBytes());
        when(storeSignedContractPdf.execute(any(Contract.class), any(byte[].class)))
                .thenReturn(new StoredContractPdf("signed.pdf", false));
        when(contractPersistenceService.markAsSigned(contract, "signed.pdf")).thenReturn(saved);

        Contract result = useCase.execute(contract);

        assertThat(result.getStatus()).isEqualTo(ContractStatus.ASSINADO);
        verify(notifyContractSigned).execute(saved);
        verify(contractSignedTaskAutomationService).createTasksForSignedContract(saved);
    }

    @Test
    void shouldCleanupCreatedSignedPdfWhenPersistenceFails() throws Exception {
        Contract contract = sentContract();
        StoredContractPdf storedPdf = new StoredContractPdf("new-signed.pdf", true);
        RuntimeException failure = new RuntimeException("db failure");

        when(checkSignatureStatus.shouldSynchronize(contract)).thenReturn(true);
        when(checkSignatureStatus.isSigned(contract)).thenReturn(true);
        when(downloadSignedContract.execute(contract)).thenReturn("%PDF signed".getBytes());
        when(storeSignedContractPdf.execute(any(Contract.class), any(byte[].class))).thenReturn(storedPdf);
        when(contractPersistenceService.markAsSigned(contract, "new-signed.pdf")).thenThrow(failure);

        assertThatThrownBy(() -> useCase.execute(contract)).isSameAs(failure);
        verify(storeSignedContractPdf).cleanupIfCreated(storedPdf);
        verify(notifyContractSigned, never()).execute(contract);
        verify(contractSignedTaskAutomationService, never()).createTasksForSignedContract(any());
    }

    private Contract sentContract() {
        Contract contract = new Contract();
        contract.markAsDraft();
        contract.attachPdf("contract.pdf");
        contract.markAsSent("autentique-id");
        return contract;
    }
}
