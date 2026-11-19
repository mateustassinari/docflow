package br.com.docflow.application.usecase;

import br.com.docflow.application.port.ContractFileStorage;
import br.com.docflow.application.service.ContractPersistenceService;
import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.TemplateContrato;
import br.com.docflow.domain.model.TipoServico;
import br.com.docflow.domain.repository.ContractRepository;
import br.com.docflow.domain.repository.EventRepository;
import br.com.docflow.domain.repository.TemplateContratoRepository;
import br.com.docflow.domain.service.ContractDocumentGenerator;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateContractUseCaseTest {

    private final ContractRepository contractRepository = mock(ContractRepository.class);
    private final EventRepository eventRepository = mock(EventRepository.class);
    private final TemplateContratoRepository templateRepository = mock(TemplateContratoRepository.class);
    private final ContractDocumentGenerator documentGenerator = mock(ContractDocumentGenerator.class);
    private final ContractFileStorage fileStorage = mock(ContractFileStorage.class);
    private final ContractPersistenceService contractPersistenceService = mock(ContractPersistenceService.class);
    private final GenerateContractUseCase useCase = new GenerateContractUseCase(
            contractRepository,
            eventRepository,
            templateRepository,
            documentGenerator,
            fileStorage,
            contractPersistenceService
    );

    @Test
    void shouldCleanupGeneratedPdfWhenPersistenceFails() throws Exception {
        Event event = event(1L, TipoServico.PINTURA_AO_VIVO);
        TemplateContrato template = template(2L, TipoServico.PINTURA_AO_VIVO);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(templateRepository.findById(2L)).thenReturn(Optional.of(template));
        when(contractRepository.findByEventId(1L)).thenReturn(Optional.empty());
        when(documentGenerator.generatePdf(any(Contract.class))).thenReturn("%PDF".getBytes());
        when(fileStorage.savePdf(any(byte[].class), any(String.class))).thenReturn("generated.pdf");
        when(contractPersistenceService.saveNewContract(any(Contract.class)))
                .thenThrow(new BusinessException("DB_ERROR", "Falha ao salvar"));

        assertThatThrownBy(() -> useCase.execute(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Falha ao salvar");

        verify(fileStorage).deletePdfIfExists("generated.pdf");
    }

    private Event event(Long id, TipoServico tipoServico) {
        Event event = new Event();
        event.setId(id);
        event.setTipoServico(tipoServico);
        return event;
    }

    private TemplateContrato template(Long id, TipoServico tipoServico) {
        TemplateContrato template = new TemplateContrato();
        template.setId(id);
        template.setTipoServico(tipoServico);
        return template;
    }
}
