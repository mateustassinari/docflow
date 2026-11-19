package br.com.docflow.presentation.dto;

import br.com.docflow.domain.model.Event;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Evento na API. Separado do domínio para o JSON não mudar sozinho quando o modelo mudar. */
public record EventResponse(
        Long id,
        ClientResponse client,
        String tipoServico,
        String dadosServico,
        String tipoEvento,
        LocalDate data,
        LocalTime horarioInicio,
        String duracaoEstimada,
        String local,
        Integer numeroConvidados,
        String tecnica,
        String tamanhoTela,
        String cenaRetratada,
        String entregaObra,
        BigDecimal valor,
        BigDecimal valorSinal,
        String formaPagamentoSinal,
        String formaPagamentoRestante,
        String observacoes,
        boolean sinalRecebido,
        LocalDate dataRecebimentoSinal,
        boolean restanteRecebido,
        LocalDate dataRecebimentoRestante,
        String observacaoPagamento,
        BigDecimal custoTotal,
        boolean contratoDispensado,
        BigDecimal custosExtras
) {

    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getClient() != null ? ClientResponse.from(event.getClient()) : null,
                event.getTipoServico() != null ? event.getTipoServico().name() : null,
                event.getDadosServico(),
                event.getTipoEvento(),
                event.getData(),
                event.getHorarioInicio(),
                event.getDuracaoEstimada(),
                event.getLocal(),
                event.getNumeroConvidados(),
                event.getTecnica(),
                event.getTamanhoTela(),
                event.getCenaRetratada(),
                event.getEntregaObra(),
                event.getValor(),
                event.getValorSinal(),
                event.getFormaPagamentoSinal(),
                event.getFormaPagamentoRestante(),
                event.getObservacoes(),
                event.isSinalRecebido(),
                event.getDataRecebimentoSinal(),
                event.isRestanteRecebido(),
                event.getDataRecebimentoRestante(),
                event.getObservacaoPagamento(),
                event.getCustoTotal(),
                event.isContratoDispensado(),
                event.getCustosExtras()
        );
    }
}
