package br.com.docflow.presentation.dto;

import br.com.docflow.application.usecase.command.EventCommand;
import br.com.docflow.application.usecase.command.UpdateEventPaymentCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * toCommand() usa construtor posicional com vários campos do mesmo tipo: cada campo recebe
 * um valor distinto para que uma troca de posição faça o teste falhar.
 */
class EventRequestTest {

    @Test
    void shouldMapEveryEventFieldToTheMatchingCommandField() {
        EventRequest request = new EventRequest();
        request.setClienteId(7L);
        request.setTipoServico("tipoServico");
        request.setDadosServico("dadosServico");
        request.setTipoEvento("tipoEvento");
        request.setData(LocalDate.of(2026, 12, 1));
        request.setHorarioInicio(LocalTime.of(18, 30));
        request.setDuracaoEstimada("duracaoEstimada");
        request.setLocal("local");
        request.setNumeroConvidados(150);
        request.setTecnica("tecnica");
        request.setTamanhoTela("tamanhoTela");
        request.setCenaRetratada("cenaRetratada");
        request.setEntregaObra("entregaObra");
        request.setValor(new BigDecimal("1000"));
        request.setValorSinal(new BigDecimal("300"));
        request.setFormaPagamentoSinal("formaPagamentoSinal");
        request.setFormaPagamentoRestante("formaPagamentoRestante");
        request.setObservacoes("observacoes");
        request.setContratoDispensado(true);

        EventCommand command = request.toCommand();

        assertThat(command).isEqualTo(new EventCommand(
                7L, "tipoServico", "dadosServico", "tipoEvento", LocalDate.of(2026, 12, 1),
                LocalTime.of(18, 30), "duracaoEstimada", "local", 150, "tecnica", "tamanhoTela",
                "cenaRetratada", "entregaObra", new BigDecimal("1000"), new BigDecimal("300"),
                "formaPagamentoSinal", "formaPagamentoRestante", "observacoes", true));
        assertThat(command.tipoServico()).isEqualTo("tipoServico");
        assertThat(command.formaPagamentoRestante()).isEqualTo("formaPagamentoRestante");
        assertThat(command.observacoes()).isEqualTo("observacoes");
    }

    @Test
    void shouldMapEveryPaymentFieldToTheMatchingCommandField() {
        EventPaymentRequest request = new EventPaymentRequest();
        request.setSinalRecebido(true);
        request.setDataRecebimentoSinal(LocalDate.of(2026, 11, 1));
        request.setRestanteRecebido(false);
        request.setDataRecebimentoRestante(LocalDate.of(2026, 11, 20));
        request.setObservacaoPagamento("obs");
        request.setCustoTotal(new BigDecimal("200"));
        request.setCustosExtras(new BigDecimal("50"));

        UpdateEventPaymentCommand command = request.toCommand();

        assertThat(command.sinalRecebido()).isTrue();
        assertThat(command.dataRecebimentoSinal()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(command.restanteRecebido()).isFalse();
        assertThat(command.dataRecebimentoRestante()).isEqualTo(LocalDate.of(2026, 11, 20));
        assertThat(command.observacaoPagamento()).isEqualTo("obs");
        assertThat(command.custoTotal()).isEqualByComparingTo("200");
        assertThat(command.custosExtras()).isEqualByComparingTo("50");
    }
}
