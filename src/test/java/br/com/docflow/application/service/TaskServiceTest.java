package br.com.docflow.application.service;

import br.com.docflow.application.readmodel.TaskSummary;
import br.com.docflow.application.readmodel.TaskView;
import br.com.docflow.application.usecase.command.AttachmentUpload;
import br.com.docflow.application.usecase.command.TaskCommand;
import br.com.docflow.application.usecase.command.UpdateTaskDeadlineCommand;
import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.ChecklistItem;
import br.com.docflow.domain.model.Client;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.Task;
import br.com.docflow.domain.model.TaskAttachment;
import br.com.docflow.domain.model.TaskAutomation;
import br.com.docflow.domain.model.TaskComment;
import br.com.docflow.domain.model.TaskStatus;
import br.com.docflow.domain.model.TipoServico;
import br.com.docflow.testsupport.InMemoryEventRepository;
import br.com.docflow.testsupport.InMemoryTaskAttachmentStorage;
import br.com.docflow.testsupport.InMemoryTaskRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskServiceTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);
    private static final Clock CLOCK = Clock.fixed(TODAY.atStartOfDay(SAO_PAULO).toInstant(), SAO_PAULO);

    private final InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
    private final InMemoryEventRepository eventRepository = new InMemoryEventRepository();
    private final InMemoryTaskAttachmentStorage storage = new InMemoryTaskAttachmentStorage();
    private final ContractSignedTaskAutomationService automation =
            new ContractSignedTaskAutomationService(taskRepository, eventRepository, new ObjectMapper(), CLOCK);
    private final TaskService service = new TaskService(taskRepository, eventRepository, storage, automation, CLOCK);

    @Test
    void createShouldNormalizeTheRequest() {
        Event event = givenEvent("Isabella", TipoServico.AQUARELA_AO_VIVO);

        TaskView view = service.create(command(event.getId(), "  Reunião  ", "   ", TODAY, TODAY, LocalTime.of(10, 0), null, null, null,
                List.of(" urgente ", "urgente", ""), List.of(new ChecklistItem(null, " Papel ", false, null))));

        Task task = view.task();
        assertThat(task.getTitulo()).isEqualTo("Reunião");
        assertThat(task.getDescricao()).as("descrição em branco vira nula").isNull();
        assertThat(task.getPrazoDataFim()).as("fim igual ao início não é período").isNull();
        assertThat(task.getPrazoHorarioFim()).as("sem horário de fim, dura 1 hora").isEqualTo(LocalTime.of(11, 0));
        assertThat(task.getStatus()).isEqualTo(TaskStatus.A_FAZER);
        assertThat(task.getTipoServico()).as("sem tipo informado, herda o do evento").isEqualTo("AQUARELA_AO_VIVO");
        assertThat(task.getEtiquetas()).containsExactly("urgente");
        assertThat(task.getChecklist()).extracting(ChecklistItem::texto).containsExactly("Papel");
        assertThat(view.event().clienteNome()).isEqualTo("Isabella");
    }

    @Test
    void endDateBeforeStartShouldBeMovedToTheStart() {
        TaskView view = service.create(command(null, "Período", null, TODAY, TODAY.minusDays(2), LocalTime.of(9, 0),
                LocalTime.of(10, 0), "em_andamento", " PINTURA_AO_VIVO ", List.of(), List.of()));

        assertThat(view.task().getPrazoDataFim()).isEqualTo(TODAY);
        assertThat(view.task().getStatus()).isEqualTo(TaskStatus.EM_ANDAMENTO);
        assertThat(view.task().getTipoServico()).isEqualTo("PINTURA_AO_VIVO");
        assertThat(view.event()).isNull();
    }

    @Test
    void invalidInputShouldFailWithTheSameBusinessErrors() {
        assertThatThrownBy(() -> service.create(command(99L, "X", null, TODAY, null, LocalTime.NOON, null, null, null, List.of(), List.of())))
                .isInstanceOf(NotFoundException.class).hasMessage("Evento não encontrado");
        assertThatThrownBy(() -> service.create(command(null, "X", null, TODAY, null, null, null, null, null, List.of(), List.of())))
                .isInstanceOf(BusinessException.class).hasMessage("O horário de início é obrigatório");
        assertThatThrownBy(() -> service.create(command(null, "X", null, TODAY, null, LocalTime.NOON, null, "FEITO", null, List.of(), List.of())))
                .isInstanceOf(BusinessException.class).hasMessage("Status de tarefa inválido: FEITO");
        assertThatThrownBy(() -> service.findById(42L))
                .isInstanceOf(NotFoundException.class).hasMessage("Tarefa não encontrada");
    }

    @Test
    void completingMolduraCaixaOnPaintingMaterialsShouldCreateConditionalTasksOnce() {
        Event event = givenEvent("Ana", TipoServico.PINTURA_AO_VIVO);
        event.setData(LocalDate.of(2026, 12, 20));
        Task material = new Task();
        material.setEventId(event.getId());
        material.setTitulo("Compra de material");
        material.setPrazoData(LocalDate.of(2026, 12, 5));
        material.setPrazoHorario(LocalTime.of(9, 0));
        material.setAutomation(new TaskAutomation("CONTRACT_SIGNED", 99L, Task.PAINTING_MATERIALS_KEY));
        Long id = taskRepository.save(material).getId();
        TaskCommand completed = command(event.getId(), "Compra de material", null, LocalDate.of(2026, 12, 5), null,
                LocalTime.of(9, 0), LocalTime.of(18, 0), null, null, List.of(),
                List.of(new ChecklistItem(null, "Tela", true, 0), new ChecklistItem(null, "Moldura e Caixa?", true, 1)));

        service.update(id, completed);
        service.update(id, completed);

        assertThat(taskRepository.all()).extracting(Task::getTitulo).containsExactly(
                "Compra de material", "Retoque Pintura", "Emoldurar e Preparar Encomenda", "Enviar Pintura");
    }

    @Test
    void molduraCaixaStillOpenShouldNotCreateConditionalTasks() {
        Event event = givenEvent("Ana", TipoServico.PINTURA_AO_VIVO);
        Task material = new Task();
        material.setEventId(event.getId());
        material.setTitulo("Compra de material");
        material.setPrazoData(TODAY);
        material.setPrazoHorario(LocalTime.of(9, 0));
        material.setAutomation(new TaskAutomation("CONTRACT_SIGNED", 99L, Task.PAINTING_MATERIALS_KEY));
        Long id = taskRepository.save(material).getId();

        service.update(id, command(event.getId(), "Compra de material", null, TODAY, null, LocalTime.of(9, 0), null, null, null,
                List.of(), List.of(new ChecklistItem(null, "Moldura e Caixa?", false, 0))));

        assertThat(taskRepository.all()).hasSize(1);
    }

    @Test
    void shouldUpdateDeadlineAndStatus() {
        Long id = givenTask(TODAY, TaskStatus.A_FAZER).getId();

        service.updateDeadline(id, new UpdateTaskDeadlineCommand(TODAY.plusDays(1), TODAY.plusDays(3), LocalTime.of(8, 0), null));
        TaskView view = service.updateStatus(id, "concluida");

        assertThat(view.task().getPrazoData()).isEqualTo(TODAY.plusDays(1));
        assertThat(view.task().getPrazoDataFim()).isEqualTo(TODAY.plusDays(3));
        assertThat(view.task().getPrazoHorarioFim()).isEqualTo(LocalTime.of(9, 0));
        assertThat(view.task().getStatus()).isEqualTo(TaskStatus.CONCLUIDA);
    }

    @Test
    void shouldAddEditAndRemoveComments() {
        Long id = givenTask(TODAY, TaskStatus.A_FAZER).getId();

        Long commentId = service.addComment(id, "  primeiro  ").task().getComments().get(0).id();
        service.updateComment(id, commentId, "editado");
        assertThat(service.findById(id).task().getComments()).extracting(TaskComment::texto).containsExactly("editado");

        service.deleteComment(id, commentId);
        assertThat(service.findById(id).task().getComments()).isEmpty();
    }

    @Test
    void attachmentFilesShouldFollowTheTaskLifecycle() throws IOException {
        Long id = givenTask(TODAY, TaskStatus.A_FAZER).getId();

        TaskAttachment first = service.uploadAttachment(id, new AttachmentUpload("a.pdf", "application/pdf", new byte[]{1, 2, 3}))
                .task().getAttachments().get(0);
        TaskAttachment second = service.uploadAttachment(id, new AttachmentUpload(null, null, new byte[]{4}))
                .task().getAttachments().get(1);

        assertThat(first.sizeBytes()).isEqualTo(3);
        assertThat(second.originalName()).as("arquivo sem nome vira \"anexo\"").isEqualTo("anexo");
        assertThat(service.loadAttachment(service.findAttachment(id, first.id()))).containsExactly(1, 2, 3);

        service.deleteAttachment(id, first.id());
        assertThat(storage.exists(first.storedPath())).as("remover o anexo apaga o arquivo").isFalse();
        assertThat(service.findById(id).task().getAttachments()).extracting(TaskAttachment::id).containsExactly(second.id());

        service.delete(id);
        assertThat(storage.exists(second.storedPath())).as("apagar a tarefa apaga os arquivos").isFalse();
        assertThat(taskRepository.all()).isEmpty();
    }

    @Test
    void summaryShouldCountOpenTasksByEffectiveDeadline() {
        givenTask(TODAY.minusDays(1), TaskStatus.A_FAZER);
        givenTask(TODAY, TaskStatus.EM_ANDAMENTO);
        givenTask(TODAY.plusDays(7), TaskStatus.A_FAZER);
        givenTask(TODAY.plusDays(8), TaskStatus.A_FAZER);
        givenTask(TODAY.minusDays(5), TaskStatus.CONCLUIDA);

        assertThat(service.summary()).isEqualTo(new TaskSummary(1, 1, 1));
    }

    @Test
    void listShouldFilterAndAttachEventSummary() {
        Event event = givenEvent("Nicole", TipoServico.PINTURA_AO_VIVO);
        Task withEvent = givenTask(TODAY, TaskStatus.A_FAZER);
        withEvent.setEventId(event.getId());
        taskRepository.save(withEvent);
        givenTask(TODAY, TaskStatus.CONCLUIDA);

        List<TaskView> views = service.list(null, null, "a_fazer", null, null, null);

        assertThat(views).singleElement().satisfies(view -> {
            assertThat(view.task().getId()).isEqualTo(withEvent.getId());
            assertThat(view.event().clienteNome()).isEqualTo("Nicole");
            assertThat(view.event().tipoServico()).isEqualTo("PINTURA_AO_VIVO");
        });
    }

    private TaskCommand command(Long eventId, String titulo, String descricao, LocalDate start, LocalDate end, LocalTime startTime,
                                LocalTime endTime, String status, String tipoServico, List<String> labels, List<ChecklistItem> checklist) {
        return new TaskCommand(eventId, titulo, descricao, start, end, startTime, endTime, status, tipoServico, labels, checklist);
    }

    private Event givenEvent(String clientName, TipoServico tipoServico) {
        Client client = new Client();
        client.setNome(clientName);
        Event event = new Event();
        event.setClient(client);
        event.setTipoServico(tipoServico);
        return eventRepository.given(event);
    }

    private Task givenTask(LocalDate deadline, TaskStatus status) {
        Task task = new Task();
        task.setTitulo("Tarefa");
        task.setPrazoData(deadline);
        task.setPrazoHorario(LocalTime.of(9, 0));
        task.setStatus(status);
        return taskRepository.save(task);
    }
}
