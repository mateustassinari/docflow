package br.com.docflow.application.usecase;

import br.com.docflow.application.port.ContractFileStorage;
import br.com.docflow.application.service.ContractPersistenceService;
import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.TemplateContrato;
import br.com.docflow.domain.repository.ContractRepository;
import br.com.docflow.domain.repository.EventRepository;
import br.com.docflow.domain.repository.TemplateContratoRepository;
import br.com.docflow.domain.service.ContractDocumentGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenerateContractUseCase {

    private final ContractRepository contractRepository;
    private final EventRepository eventRepository;
    private final TemplateContratoRepository templateRepository;
    private final ContractDocumentGenerator documentGenerator;
    private final ContractFileStorage fileStorage;
    private final ContractPersistenceService contractPersistenceService;

    public Contract execute(Long eventId, Long templateId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("EVENT_NOT_FOUND", "Evento não encontrado"));

        if (templateId == null) {
            throw new BusinessException("TEMPLATE_REQUIRED", "Selecione um template para gerar o contrato");
        }

        TemplateContrato template = templateRepository.findById(templateId)
                .orElseThrow(() -> new NotFoundException("TEMPLATE_NOT_FOUND", "Template nao encontrado"));

        if (!isTemplateCompatible(event, template)) {
            throw new BusinessException(
                    "TEMPLATE_SERVICE_MISMATCH",
                    "O template selecionado nao corresponde ao servico informado no evento"
            );
        }

        contractRepository.findByEventId(eventId).ifPresent(existing -> {
            throw new BusinessException("CONTRACT_ALREADY_EXISTS", "Este evento já possui contrato gerado");
        });

        Contract contract = new Contract();
        contract.setEvent(event);
        contract.setTemplate(template);
        contract.markAsDraft();

        try {
            byte[] generatedPdf = documentGenerator.generatePdf(contract);
            String pdfUrl = fileStorage.savePdf(generatedPdf, "contract_event_" + event.getId());
            contract.attachPdf(pdfUrl);
            try {
                return contractPersistenceService.saveNewContract(contract);
            } catch (RuntimeException persistenceError) {
                cleanupCreatedPdf(pdfUrl);
                throw persistenceError;
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("DOCUMENT_GENERATION_FAILED", "Falha ao gerar o documento PDF: " + e.getMessage(), e);
        }
    }

    private boolean isTemplateCompatible(Event event, TemplateContrato template) {
        if (event != null && template != null && event.getTipoServico() != null && template.getTipoServico() != null) {
            return event.getTipoServico() == template.getTipoServico();
        }

        return true;
    }

    private void cleanupCreatedPdf(String pdfUrl) {
        try {
            fileStorage.deletePdfIfExists(pdfUrl);
        } catch (Exception ignored) {
            // A falha de limpeza nao deve esconder o erro original de persistencia.
        }
    }
}
