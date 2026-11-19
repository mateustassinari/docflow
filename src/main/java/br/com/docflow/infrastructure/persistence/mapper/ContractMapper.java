package br.com.docflow.infrastructure.persistence.mapper;

import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.ContractStatus;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.TipoServico;
import br.com.docflow.infrastructure.persistence.entity.ContractEntity;
import br.com.docflow.infrastructure.persistence.entity.EventEntity;
import br.com.docflow.infrastructure.persistence.entity.TemplateContratoEntity;

public class ContractMapper {

    public static ContractEntity toEntity(Contract contract) {
        if (contract == null) return null;
        
        ContractEntity entity = new ContractEntity();
        entity.setId(contract.getId());
        
        if (contract.getStatus() != null) {
            entity.setStatus(contract.getStatus().name());
        }
        
        entity.setPdfUrl(contract.getPdfUrl());
        entity.setAutentiqueId(contract.getAutentiqueId());
        if (contract.getTemplate() != null) {
            TemplateContratoEntity templateRef = new TemplateContratoEntity();
            templateRef.setId(contract.getTemplate().getId());
            entity.setTemplate(templateRef);
        }
        
        if (contract.getEvent() != null) {
            EventEntity eventRef = new EventEntity();
            eventRef.setId(contract.getEvent().getId());
            entity.setEvent(eventRef);
        }
        
        return entity;
    }

    public static Contract toDomain(ContractEntity entity) {
        if (entity == null) return null;

        ContractStatus status = null;
        if (entity.getStatus() != null) {
            status = ContractStatus.valueOf(entity.getStatus());
        }

        Event event = null;
        if (entity.getEvent() != null) {
            event = new Event();
            event.setId(entity.getEvent().getId());
            event.setClient(ClientMapper.toDomain(entity.getEvent().getClient()));
            event.setTipoServico(parseTipoServico(entity.getEvent().getTipoServico()));
            event.setDadosServico(entity.getEvent().getDadosServico());
            event.setTipoEvento(entity.getEvent().getTipoEvento());
            event.setData(entity.getEvent().getData());
            event.setHorarioInicio(entity.getEvent().getHorarioInicio());
            event.setDuracaoEstimada(entity.getEvent().getDuracaoEstimada());
            event.setLocal(entity.getEvent().getLocal());
            event.setNumeroConvidados(entity.getEvent().getNumeroConvidados());
            event.setTecnica(entity.getEvent().getTecnica());
            event.setTamanhoTela(entity.getEvent().getTamanhoTela());
            event.setCenaRetratada(entity.getEvent().getCenaRetratada());
            event.setEntregaObra(entity.getEvent().getEntregaObra());
            event.setValor(entity.getEvent().getValor());
            event.setValorSinal(entity.getEvent().getValorSinal());
            event.setFormaPagamentoSinal(entity.getEvent().getFormaPagamentoSinal());
            event.setFormaPagamentoRestante(entity.getEvent().getFormaPagamentoRestante());
            event.setObservacoes(entity.getEvent().getObservacoes());
        }

        return Contract.restore(
                entity.getId(),
                event,
                TemplateContratoMapper.toDomain(entity.getTemplate()),
                status,
                entity.getPdfUrl(),
                entity.getAutentiqueId()
        );
    }

    private static TipoServico parseTipoServico(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return TipoServico.valueOf(value);
    }
}
