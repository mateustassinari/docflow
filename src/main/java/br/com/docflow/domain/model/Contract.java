package br.com.docflow.domain.model;

import br.com.docflow.domain.exception.BusinessException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Contract {
    private Long id;
    private Event event;
    private TemplateContrato template;

    @Setter(AccessLevel.NONE)
    private ContractStatus status;

    @Setter(AccessLevel.NONE)
    private String pdfUrl;

    @Setter(AccessLevel.NONE)
    private String autentiqueId;

    public static Contract restore(
            Long id,
            Event event,
            TemplateContrato template,
            ContractStatus status,
            String pdfUrl,
            String autentiqueId
    ) {
        Contract contract = new Contract();
        contract.setId(id);
        contract.setEvent(event);
        contract.setTemplate(template);
        contract.status = status;
        contract.pdfUrl = pdfUrl;
        contract.autentiqueId = autentiqueId;
        return contract;
    }

    public void markAsDraft() {
        if (ContractStatus.ASSINADO.equals(this.status)) {
            throw new BusinessException("CONTRACT_ALREADY_SIGNED", "Contrato assinado nao pode voltar para rascunho");
        }

        this.status = ContractStatus.RASCUNHO;
        this.autentiqueId = null;
    }

    public void markAsManuallySigned() {
        this.status = ContractStatus.ASSINADO;
        this.autentiqueId = null;
    }

    public void attachPdf(String pdfUrl) {
        if (pdfUrl == null || pdfUrl.isBlank()) {
            throw new BusinessException("CONTRACT_PDF_MISSING", "PDF do contrato nao informado");
        }

        this.pdfUrl = pdfUrl;
    }
    
    public void markAsSent(String autentiqueId) {
        if (ContractStatus.ASSINADO.equals(this.status)) {
            throw new BusinessException("CONTRACT_ALREADY_SIGNED", "Contrato assinado nao pode ser reenviado");
        }

        if (this.pdfUrl == null || this.pdfUrl.isBlank()) {
            throw new BusinessException("CONTRACT_PDF_MISSING", "PDF nao foi gerado para este contrato ainda");
        }

        if (autentiqueId == null || autentiqueId.isBlank()) {
            throw new BusinessException("SIGNATURE_ID_REQUIRED", "Identificador do Autentique nao informado");
        }

        this.status = ContractStatus.ENVIADO;
        this.autentiqueId = autentiqueId;
    }
    
    public void markAsSigned() {
        this.status = ContractStatus.ASSINADO;
    }

    public void markAsDraftForReview() {
        if (ContractStatus.ASSINADO.equals(this.status)) {
            throw new BusinessException("CONTRACT_ALREADY_SIGNED", "Contrato assinado nao pode ser revisado");
        }

        this.status = ContractStatus.RASCUNHO;
        this.autentiqueId = null;
    }
}
