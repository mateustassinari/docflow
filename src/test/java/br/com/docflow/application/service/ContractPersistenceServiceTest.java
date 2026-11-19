package br.com.docflow.application.service;

import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.repository.ContractRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContractPersistenceServiceTest {

    private final ContractRepository contractRepository = mock(ContractRepository.class);
    private final ContractPersistenceService service = new ContractPersistenceService(contractRepository);

    @Test
    void shouldSaveNewContractWhenEventHasNoContract() {
        Contract contract = contractWithEvent(10L);
        Contract saved = Contract.restore(1L, contract.getEvent(), null, contract.getStatus(), contract.getPdfUrl(), null);

        when(contractRepository.findByEventId(10L)).thenReturn(Optional.empty());
        when(contractRepository.save(contract)).thenReturn(saved);

        Contract result = service.saveNewContract(contract);

        assertThat(result.getId()).isEqualTo(1L);
        verify(contractRepository).save(contract);
    }

    @Test
    void shouldRejectNewContractWhenEventAlreadyHasContract() {
        Contract contract = contractWithEvent(10L);
        Contract existing = Contract.restore(99L, contract.getEvent(), null, contract.getStatus(), contract.getPdfUrl(), null);

        when(contractRepository.findByEventId(10L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.saveNewContract(contract))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ja possui contrato");
        verify(contractRepository, never()).save(contract);
    }

    private Contract contractWithEvent(Long eventId) {
        Event event = new Event();
        event.setId(eventId);

        Contract contract = new Contract();
        contract.setEvent(event);
        contract.markAsDraft();
        contract.attachPdf("contract.pdf");
        return contract;
    }
}
