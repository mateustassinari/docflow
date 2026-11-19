package br.com.docflow.application.usecase;

import br.com.docflow.application.port.ContractFileStorage;
import br.com.docflow.application.service.ContractSignedTaskAutomationService;
import br.com.docflow.application.service.ContractPersistenceService;
import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.TemplateContrato;
import br.com.docflow.domain.repository.ContractRepository;
import br.com.docflow.domain.repository.EventRepository;
import br.com.docflow.domain.repository.TemplateContratoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class UploadContractPdfUseCase {

    private final ContractRepository contractRepository;
    private final EventRepository eventRepository;
    private final TemplateContratoRepository templateRepository;
    private final ContractFileStorage fileStorage;
    private final ContractPersistenceService contractPersistenceService;
    private final ContractSignedTaskAutomationService contractSignedTaskAutomationService;

    public Contract execute(Long eventId, Long templateId, byte[] pdfBytes, boolean signed) {
        if (eventId == null) {
            throw new BusinessException("EVENT_REQUIRED", "Selecione um evento para vincular o contrato");
        }

        if (templateId == null) {
            throw new BusinessException("TEMPLATE_REQUIRED", "Selecione um template para vincular ao contrato");
        }

        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new BusinessException("PDF_REQUIRED", "Selecione um arquivo PDF para upload");
        }

        if (!isPdf(pdfBytes)) {
            throw new BusinessException("INVALID_PDF", "O arquivo enviado precisa ser um PDF valido");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("EVENT_NOT_FOUND", "Evento nao encontrado"));

        TemplateContrato template = templateRepository.findById(templateId)
                .orElseThrow(() -> new NotFoundException("TEMPLATE_NOT_FOUND", "Template nao encontrado"));

        contractRepository.findByEventId(eventId).ifPresent(existing -> {
            throw new BusinessException("CONTRACT_ALREADY_EXISTS", "Este evento ja possui contrato gerado");
        });

        try {
            Contract contract = new Contract();
            contract.setEvent(event);
            contract.setTemplate(template);
            String pdfUrl = fileStorage.savePdf(pdfBytes, "manual_contract_event_" + event.getId());
            contract.attachPdf(pdfUrl);
            if (signed) {
                contract.markAsManuallySigned();
            } else {
                contract.markAsDraft();
            }
            Contract saved;
            try {
                saved = contractPersistenceService.saveNewContract(contract);
            } catch (RuntimeException persistenceError) {
                cleanupCreatedPdf(pdfUrl);
                throw persistenceError;
            }
            if (signed) {
                contractSignedTaskAutomationService.createTasksForSignedContract(saved);
            }
            return saved;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("PDF_UPLOAD_FAILED", "Falha ao salvar o PDF enviado: " + e.getMessage(), e);
        }
    }

    private boolean isPdf(byte[] bytes) {
        if (bytes.length < 4) {
            return false;
        }

        String header = new String(bytes, 0, 4, StandardCharsets.US_ASCII);
        return "%PDF".equals(header);
    }

    private void cleanupCreatedPdf(String pdfUrl) {
        try {
            fileStorage.deletePdfIfExists(pdfUrl);
        } catch (Exception ignored) {
            // A falha de limpeza nao deve esconder o erro original de persistencia.
        }
    }
}
