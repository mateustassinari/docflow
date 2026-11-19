package br.com.docflow.application.service;

import br.com.docflow.domain.model.ChecklistItem;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.ContractStatus;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.Task;
import br.com.docflow.domain.model.TaskAutomation;
import br.com.docflow.domain.model.TipoServico;
import br.com.docflow.testsupport.InMemoryEventRepository;
import br.com.docflow.testsupport.InMemoryTaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fixa as tarefas, prazos e checklists que a automação pós-assinatura gera.
 * Cada tarefa é descrita como "CHAVE | título | início | fim | checklist".
 * Os valores esperados são os mesmos dos testes de caracterização escritos antes da refatoração.
 */
class ContractSignedTaskAutomationServiceTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);
    private static final LocalDate EVENT_DATE = LocalDate.of(2026, 8, 30);

    private final InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
    private final InMemoryEventRepository eventRepository = new InMemoryEventRepository();
    private final ContractSignedTaskAutomationService service = new ContractSignedTaskAutomationService(
            taskRepository,
            eventRepository,
            new ObjectMapper(),
            Clock.fixed(TODAY.atStartOfDay(SAO_PAULO).toInstant(), SAO_PAULO));

    @Test
    void paintingShouldCreateBaseTasksRelativeToEventDate() {
        Event event = givenEvent(TipoServico.PINTURA_AO_VIVO, EVENT_DATE, null);

        service.createTasksForSignedContract(contract(event));

        assertThat(describe(taskRepository.all())).containsExactly(
                "PINTURA_REUNIAO_NOIVOS | Reunião com os noivos | 2026-07-31 | - | []",
                "PINTURA_HOSPEDAGEM | Organizar hospedagem | 2026-08-15 | - | []",
                "PINTURA_COMPRA_MATERIAL | Compra de material | 2026-08-15 | - | [Tela, Tintas, Moldura e Caixa?]",
                "PINTURA_INFRAESTRUTURA | Confirmar infraestrutura com cliente/cerimonialista | 2026-08-23 | - | []");
    }

    @Test
    void paintingConditionalTasksShouldBeRelativeToEventDate() {
        Event event = givenEvent(TipoServico.PINTURA_AO_VIVO, EVENT_DATE, null);
        Task materialTask = new Task();
        materialTask.setEventId(event.getId());
        materialTask.setAutomation(new TaskAutomation("CONTRACT_SIGNED", 99L, Task.PAINTING_MATERIALS_KEY));

        service.createPaintingConditionalTasks(materialTask);

        assertThat(describe(taskRepository.all())).containsExactly(
                "PINTURA_RETOQUE | Retoque Pintura | 2026-09-01 | - | []",
                "PINTURA_EMOLDURAR_PREPARAR | Emoldurar e Preparar Encomenda | 2026-09-04 | - | []",
                "PINTURA_ENVIAR | Enviar Pintura | 2026-09-09 | - | []");
    }

    @Test
    void conditionalTasksShouldOnlyComeFromThePaintingMaterialsTask() {
        Event event = givenEvent(TipoServico.PINTURA_AO_VIVO, EVENT_DATE, null);
        Task otherTask = new Task();
        otherTask.setEventId(event.getId());
        otherTask.setAutomation(new TaskAutomation("CONTRACT_SIGNED", 99L, "PINTURA_HOSPEDAGEM"));

        service.createPaintingConditionalTasks(otherTask);
        service.createPaintingConditionalTasks(new Task());

        assertThat(taskRepository.all()).isEmpty();
    }

    @Test
    void watercolorShouldCreateBaseTasksRelativeToEventDate() {
        Event event = givenEvent(TipoServico.AQUARELA_AO_VIVO, EVENT_DATE, null);

        service.createTasksForSignedContract(contract(event));

        assertThat(describe(taskRepository.all())).containsExactly(
                "AQUARELA_REUNIAO_NOIVOS | Reunião com os noivos | 2026-07-31 | - | []",
                "AQUARELA_HOSPEDAGEM | Organizar hospedagem | 2026-08-15 | - | []",
                "AQUARELA_COMPRA_MATERIAL | Compra de material | 2026-08-15 | - | [Papel, Impressão, Molduras]",
                "AQUARELA_INFRAESTRUTURA | Confirmar infraestrutura com cliente/cerimonialista | 2026-08-23 | - | []");
    }

    @Test
    void identityShouldCountFromSigningDateAndMoveEventToDeliveryInBusinessDays() {
        Event event = givenEvent(TipoServico.IDENTIDADE_VISUAL, EVENT_DATE, "{\"prazoDiasUteis\":\"5\"}");
        LocalDate delivery = plusBusinessDays(TODAY, 5);

        service.createTasksForSignedContract(contract(event));

        assertThat(describe(taskRepository.all())).containsExactly(
                "IDENTIDADE_BRIEFING | Mandar Briefing | " + TODAY.plusDays(1) + " | - | []",
                "IDENTIDADE_PRIMEIRA_REUNIAO | Fazer primeira reunião | " + TODAY.plusDays(15) + " | - | []",
                "IDENTIDADE_PESQUISA_VISUAL | Iniciar pesquisa visual e direção criativa | " + TODAY.plusDays(17) + " | - | []",
                "IDENTIDADE_PRIMEIRA_APRESENTACAO | Preparar primeira apresentação | " + TODAY.plusDays(22) + " | - | []",
                "IDENTIDADE_PRIMEIRA_ENTREGA | Primeira Entrega | " + delivery + " | - | []");
        assertThat(eventRepository.findById(event.getId()).orElseThrow().getData())
                .as("a data do evento passa a ser a entrega")
                .isEqualTo(delivery);
        assertThat(eventRepository.saveCount()).isEqualTo(1);
    }

    @Test
    void identityWithoutDeadlineShouldDeliverIn90Days() {
        Event event = givenEvent(TipoServico.IDENTIDADE_VISUAL, EVENT_DATE, "{}");

        service.createTasksForSignedContract(contract(event));

        assertThat(describe(taskRepository.all())).last()
                .isEqualTo("IDENTIDADE_PRIMEIRA_ENTREGA | Primeira Entrega | " + TODAY.plusDays(90) + " | - | []");
    }

    @Test
    void commissionShouldCountFromSigningDateAndDeliverThreeDaysBeforeEstimatedDeadline() {
        Event event = givenEvent(TipoServico.ENCOMENDA_ARTISTICA, EVENT_DATE, "{\"prazoEstimado\":\"15 dias úteis\"}");
        LocalDate delivery = plusBusinessDays(TODAY, 15).minusDays(3);

        service.createTasksForSignedContract(contract(event));

        assertThat(describe(taskRepository.all())).containsExactly(
                "ENCOMENDA_PROJETO | Fazer projeto | " + TODAY.plusDays(2) + " | - | []",
                "ENCOMENDA_COMPRA_MATERIAL | Compra de material | " + TODAY.plusDays(7)
                        + " | - | [Tela, Tecido, Papel, Tinta, Linha, Bastidor, Moldura, Alça]",
                "ENCOMENDA_PRODUCAO | Produção | " + TODAY.plusDays(10) + " | " + TODAY.plusDays(15) + " | []",
                "ENCOMENDA_ENTREGA_ENVIO | Entrega/Envio | " + delivery + " | - | []");
        assertThat(eventRepository.findById(event.getId()).orElseThrow().getData()).isEqualTo(delivery);
    }

    @Test
    void commissionDeadlineShouldAcceptDaysAndMonthsAndDefaultTo21Days() {
        assertThat(commissionDelivery("{\"prazoEstimado\":\"10 dias\"}")).isEqualTo(TODAY.plusDays(10).minusDays(3));
        assertThat(commissionDelivery("{\"prazoEstimado\":\"2 meses\"}")).isEqualTo(TODAY.plusMonths(2).minusDays(3));
        assertThat(commissionDelivery("{\"prazoEstimado\":\"1 mês\"}")).isEqualTo(TODAY.plusMonths(1).minusDays(3));
        assertThat(commissionDelivery("{\"prazoEstimado\":\"a combinar\"}")).isEqualTo(TODAY.plusDays(21));
        assertThat(commissionDelivery(null)).isEqualTo(TODAY.plusDays(21));
    }

    @Test
    void everyAutomaticTaskShouldShareTheSameDefaults() {
        Event event = givenEvent(TipoServico.PINTURA_AO_VIVO, EVENT_DATE, null);

        service.createTasksForSignedContract(contract(event));

        assertThat(taskRepository.all()).allSatisfy(task -> {
            assertThat(task.getDescricao()).isEqualTo("Tarefa criada automaticamente após assinatura do contrato.");
            assertThat(task.getPrazoHorario()).isEqualTo(LocalTime.of(9, 0));
            assertThat(task.getPrazoHorarioFim()).isEqualTo(LocalTime.of(18, 0));
            assertThat(task.isAllDay()).isFalse();
            assertThat(task.getTipoServico()).isEqualTo("PINTURA_AO_VIVO");
            assertThat(task.getAutomation().source()).isEqualTo("CONTRACT_SIGNED");
            assertThat(task.getAutomation().contractId()).isEqualTo(99L);
            assertThat(task.getEventId()).isEqualTo(event.getId());
            assertThat(task.getChecklist()).allSatisfy(item -> assertThat(item.concluido()).isFalse());
        });
    }

    @Test
    void eventWithoutDateShouldUseTomorrowAsReference() {
        Event event = givenEvent(TipoServico.PINTURA_AO_VIVO, null, null);

        service.createTasksForSignedContract(contract(event));

        assertThat(taskRepository.all().get(0).getPrazoData()).isEqualTo(TODAY.plusDays(1).minusDays(30));
    }

    @Test
    void secondSynchronizationShouldNotDuplicateAnyTask() {
        Event event = givenEvent(TipoServico.PINTURA_AO_VIVO, EVENT_DATE, null);

        service.createTasksForSignedContract(contract(event));
        service.createTasksForSignedContract(contract(event));

        assertThat(taskRepository.all()).hasSize(4);
    }

    @Test
    void eventWithoutServiceTypeShouldCreateNothing() {
        Event event = givenEvent(null, EVENT_DATE, null);

        service.createTasksForSignedContract(contract(event));

        assertThat(taskRepository.all()).isEmpty();
    }

    @Test
    void contractWithoutEventShouldCreateNothing() {
        service.createTasksForSignedContract(Contract.restore(99L, null, null, ContractStatus.ASSINADO, "pdf", null));

        assertThat(taskRepository.all()).isEmpty();
    }

    private LocalDate commissionDelivery(String dadosServico) {
        InMemoryTaskRepository tasks = new InMemoryTaskRepository();
        InMemoryEventRepository events = new InMemoryEventRepository();
        Event event = events.given(event(TipoServico.ENCOMENDA_ARTISTICA, EVENT_DATE, dadosServico));

        new ContractSignedTaskAutomationService(tasks, events, new ObjectMapper(),
                Clock.fixed(TODAY.atStartOfDay(SAO_PAULO).toInstant(), SAO_PAULO))
                .createTasksForSignedContract(contract(event));

        return tasks.all().stream()
                .filter(task -> "ENCOMENDA_ENTREGA_ENVIO".equals(task.getAutomation().key()))
                .findFirst()
                .orElseThrow()
                .getPrazoData();
    }

    private Event givenEvent(TipoServico tipoServico, LocalDate date, String dadosServico) {
        return eventRepository.given(event(tipoServico, date, dadosServico));
    }

    private List<String> describe(List<Task> tasks) {
        return tasks.stream()
                .map(task -> String.join(" | ",
                        task.getAutomation().key(),
                        task.getTitulo(),
                        String.valueOf(task.getPrazoData()),
                        task.getPrazoDataFim() == null ? "-" : task.getPrazoDataFim().toString(),
                        task.getChecklist().stream().map(ChecklistItem::texto).toList().toString()))
                .toList();
    }

    /** Implementação independente da do serviço: pula sábados e domingos. */
    private LocalDate plusBusinessDays(LocalDate start, int days) {
        LocalDate date = start;
        int added = 0;
        while (added < days) {
            date = date.plusDays(1);
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                added++;
            }
        }
        return date;
    }

    private Contract contract(Event event) {
        return Contract.restore(99L, event, null, ContractStatus.ASSINADO, "signed.pdf", "autentique-id");
    }

    private Event event(TipoServico tipoServico, LocalDate date, String dadosServico) {
        Event event = new Event();
        event.setTipoServico(tipoServico);
        event.setData(date);
        event.setDadosServico(dadosServico);
        return event;
    }
}
