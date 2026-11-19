package br.com.docflow.application.service;

import br.com.docflow.domain.model.Client;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.TipoServico;
import br.com.docflow.domain.repository.ContractRepository;
import br.com.docflow.domain.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiContextBuilderTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);

    private final EventRepository eventRepository = mock(EventRepository.class);
    private final ContractRepository contractRepository = mock(ContractRepository.class);
    private final TaskService taskService = mock(TaskService.class);
    private final Clock clock = Clock.fixed(TODAY.atStartOfDay(SAO_PAULO).toInstant(), SAO_PAULO);
    private final AiContextBuilder builder = new AiContextBuilder(eventRepository, contractRepository, taskService, clock);

    private String context;

    @BeforeEach
    void setUp() {
        // Evento passado, sinal recebido em setembro e restante em aberto: 700 em atraso.
        Event pastUnpaid = event(1L, "Ana", LocalDate.of(2026, 10, 10), "1000", "300");
        pastUnpaid.setSinalRecebido(true);
        pastUnpaid.setDataRecebimentoSinal(LocalDate.of(2026, 9, 1));

        // Evento futuro, sinal recebido neste mês: 500 entra no mês, 1500 ainda vai vencer.
        Event futureWithSignal = event(2L, "Bia", LocalDate.of(2026, 12, 20), "2000", "500");
        futureWithSignal.setSinalRecebido(true);
        futureWithSignal.setDataRecebimentoSinal(LocalDate.of(2026, 11, 5));

        // Evento quitado neste mês, único com custo informado.
        Event paidWithCost = event(3L, "Carla", LocalDate.of(2026, 11, 2), "800", null);
        paidWithCost.setRestanteRecebido(true);
        paidWithCost.setDataRecebimentoRestante(LocalDate.of(2026, 11, 3));
        paidWithCost.setCustoTotal(new BigDecimal("200"));

        when(eventRepository.findAll()).thenReturn(List.of(pastUnpaid, futureWithSignal, paidWithCost));
        when(contractRepository.findAll()).thenReturn(List.of());
        when(taskService.list(any(), any(), any(), any(), any(), any())).thenReturn(List.of());

        context = builder.build();
    }

    @Test
    void shouldCountMonthlyReceiptsByReceiptDateNotEventDate() {
        assertThat(context)
                .as("recebido no mês deve somar o sinal da Bia (500, recebido em 05/11) e o restante da Carla (800), "
                        + "e não o sinal da Ana, recebido em setembro")
                .contains("Recebido no mês atual (pela data de recebimento): R$ 1300.00");
    }

    @Test
    void shouldSeparateOverdueFromNotYetDue() {
        assertThat(context)
                .contains("Em atraso (evento já realizado e não quitado): R$ 700.00")
                .contains("A receber de eventos futuros (ainda não vencido): R$ 1500.00");

        String overdueGroup = context.substring(context.indexOf("EM ATRASO"), context.indexOf("A VENCER"));
        assertThat(overdueGroup)
                .as("evento futuro não pode aparecer como cobrança em atraso")
                .contains("Ana")
                .doesNotContain("Bia");
    }

    @Test
    void shouldComputeMarginOnlyForEventsWithCostInformed() {
        assertThat(context)
                .contains("Eventos sem custo informado (fora da margem): 2")
                .contains("receita R$ 800.00 | custo R$ 200.00 | margem R$ 600.00")
                .as("eventos sem custo não podem inflar a margem")
                .doesNotContain("receita R$ 3800.00");
    }

    @Test
    void shouldNotListFutureMonthsInHistory() {
        String history = context.substring(context.indexOf("== HISTÓRICO"), context.indexOf("== TAREFAS"));
        assertThat(history)
                .contains("2026-10", "2026-11")
                .as("mês futuro é agenda, não histórico")
                .doesNotContain("2026-12");
    }

    private Event event(Long id, String clientName, LocalDate date, String valor, String valorSinal) {
        Client client = new Client();
        client.setId(id);
        client.setNome(clientName);

        Event event = new Event();
        event.setId(id);
        event.setClient(client);
        event.setTipoServico(TipoServico.PINTURA_AO_VIVO);
        event.setTipoEvento("Casamento");
        event.setData(date);
        event.setValor(new BigDecimal(valor));
        event.setValorSinal(valorSinal == null ? null : new BigDecimal(valorSinal));
        return event;
    }
}
