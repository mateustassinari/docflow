package br.com.docflow.infrastructure.persistence.mapper;

import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.TipoServico;
import br.com.docflow.infrastructure.persistence.entity.EventEntity;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public Event toDomain(EventEntity entity) {
        if (entity == null) {
            return null;
        }

        Event event = new Event();
        event.setId(entity.getId());
        event.setClient(ClientMapper.toDomain(entity.getClient()));
        event.setTipoServico(parseTipoServico(entity.getTipoServico()));
        event.setDadosServico(entity.getDadosServico());
        event.setTipoEvento(entity.getTipoEvento());
        event.setData(entity.getData());
        event.setHorarioInicio(entity.getHorarioInicio());
        event.setDuracaoEstimada(entity.getDuracaoEstimada());
        event.setLocal(entity.getLocal());
        event.setNumeroConvidados(entity.getNumeroConvidados());
        event.setTecnica(entity.getTecnica());
        event.setTamanhoTela(entity.getTamanhoTela());
        event.setCenaRetratada(entity.getCenaRetratada());
        event.setEntregaObra(entity.getEntregaObra());
        event.setValor(entity.getValor());
        event.setValorSinal(entity.getValorSinal());
        event.setFormaPagamentoSinal(entity.getFormaPagamentoSinal());
        event.setFormaPagamentoRestante(entity.getFormaPagamentoRestante());
        event.setObservacoes(entity.getObservacoes());
        event.setSinalRecebido(entity.isSinalRecebido());
        event.setDataRecebimentoSinal(entity.getDataRecebimentoSinal());
        event.setRestanteRecebido(entity.isRestanteRecebido());
        event.setDataRecebimentoRestante(entity.getDataRecebimentoRestante());
        event.setObservacaoPagamento(entity.getObservacaoPagamento());
        event.setCustoTotal(entity.getCustoTotal());
        event.setContratoDispensado(entity.isContratoDispensado());
        event.setCustosExtras(entity.getCustosExtras());

        return event;
    }

    public EventEntity toEntity(Event domain) {
        if (domain == null) {
            return null;
        }

        EventEntity entity = new EventEntity();
        entity.setId(domain.getId());
        entity.setClient(ClientMapper.toEntity(domain.getClient()));
        entity.setTipoServico(domain.getTipoServico() != null ? domain.getTipoServico().name() : null);
        entity.setDadosServico(domain.getDadosServico());
        entity.setTipoEvento(domain.getTipoEvento());
        entity.setData(domain.getData());
        entity.setHorarioInicio(domain.getHorarioInicio());
        entity.setDuracaoEstimada(domain.getDuracaoEstimada());
        entity.setLocal(domain.getLocal());
        entity.setNumeroConvidados(domain.getNumeroConvidados());
        entity.setTecnica(domain.getTecnica());
        entity.setTamanhoTela(domain.getTamanhoTela());
        entity.setCenaRetratada(domain.getCenaRetratada());
        entity.setEntregaObra(domain.getEntregaObra());
        entity.setValor(domain.getValor());
        entity.setValorSinal(domain.getValorSinal());
        entity.setFormaPagamentoSinal(domain.getFormaPagamentoSinal());
        entity.setFormaPagamentoRestante(domain.getFormaPagamentoRestante());
        entity.setObservacoes(domain.getObservacoes());
        entity.setSinalRecebido(domain.isSinalRecebido());
        entity.setDataRecebimentoSinal(domain.getDataRecebimentoSinal());
        entity.setRestanteRecebido(domain.isRestanteRecebido());
        entity.setDataRecebimentoRestante(domain.getDataRecebimentoRestante());
        entity.setObservacaoPagamento(domain.getObservacaoPagamento());
        entity.setCustoTotal(domain.getCustoTotal());
        entity.setContratoDispensado(domain.isContratoDispensado());
        entity.setCustosExtras(domain.getCustosExtras());

        return entity;
    }

    private TipoServico parseTipoServico(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return TipoServico.valueOf(value);
    }
}
