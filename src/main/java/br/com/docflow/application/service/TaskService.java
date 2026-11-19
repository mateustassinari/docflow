package br.com.docflow.application.service;

import br.com.docflow.application.port.TaskAttachmentStorage;
import br.com.docflow.application.readmodel.TaskEventSummary;
import br.com.docflow.application.readmodel.TaskSummary;
import br.com.docflow.application.readmodel.TaskView;
import br.com.docflow.application.usecase.command.AttachmentUpload;
import br.com.docflow.application.usecase.command.TaskCommand;
import br.com.docflow.application.usecase.command.UpdateTaskDeadlineCommand;
import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.Task;
import br.com.docflow.domain.model.TaskAttachment;
import br.com.docflow.domain.model.TaskFilter;
import br.com.docflow.domain.model.TaskStatus;
import br.com.docflow.domain.repository.EventRepository;
import br.com.docflow.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final TaskAttachmentStorage attachmentStorage;
    private final ContractSignedTaskAutomationService contractSignedTaskAutomationService;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<TaskView> list(LocalDate start, LocalDate end, String status, String tipoServico, Long eventId, String label) {
        TaskStatus parsedStatus = status != null && !status.isBlank() ? parseStatus(status) : null;
        return views(taskRepository.search(new TaskFilter(start, end, parsedStatus, tipoServico, eventId, label)));
    }

    @Transactional(readOnly = true)
    public List<TaskView> listByEvent(Long eventId) {
        return views(taskRepository.findByEventId(eventId));
    }

    @Transactional(readOnly = true)
    public TaskView findById(Long id) {
        return view(findTask(id));
    }

    @Transactional
    public TaskView create(TaskCommand command) {
        Task task = new Task();
        apply(task, command);
        return view(taskRepository.save(task));
    }

    /**
     * Ao concluir "Moldura e Caixa" na tarefa de compra de material da pintura, cria as tarefas
     * seguintes (retoque, emoldurar, envio). A criação é idempotente, então reenviar o mesmo
     * checklist não duplica nada.
     */
    @Transactional
    public TaskView update(Long id, TaskCommand command) {
        Task task = findTask(id);
        apply(task, command);
        Task saved = taskRepository.save(task);
        if (saved.isPaintingMaterialsAutomation() && saved.hasCompletedMolduraCaixa()) {
            contractSignedTaskAutomationService.createPaintingConditionalTasks(saved);
        }
        return view(saved);
    }

    @Transactional
    public void delete(Long id) throws IOException {
        Task task = findTask(id);
        for (TaskAttachment attachment : task.getAttachments()) {
            attachmentStorage.deleteIfExists(attachment.storedPath());
        }
        taskRepository.deleteById(id);
    }

    @Transactional
    public TaskView updateDeadline(Long id, UpdateTaskDeadlineCommand command) {
        Task task = findTask(id);
        task.setPrazoData(command.prazoData());
        task.setPrazoDataFim(normalizeEndDate(command.prazoData(), command.prazoDataFim()));
        task.setPrazoHorario(requireStartTime(command.prazoHorario()));
        task.setPrazoHorarioFim(normalizeEndTime(command.prazoHorario(), command.prazoHorarioFim()));
        task.setAllDay(false);
        return view(taskRepository.save(task));
    }

    @Transactional
    public TaskView updateStatus(Long id, String status) {
        Task task = findTask(id);
        task.setStatus(parseStatus(status));
        return view(taskRepository.save(task));
    }

    @Transactional
    public TaskView addComment(Long id, String texto) {
        Task task = findTask(id);
        task.addComment(texto);
        return view(taskRepository.save(task));
    }

    @Transactional
    public TaskView updateComment(Long id, Long commentId, String texto) {
        Task task = findTask(id);
        task.updateComment(commentId, texto);
        return view(taskRepository.save(task));
    }

    @Transactional
    public TaskView deleteComment(Long id, Long commentId) {
        Task task = findTask(id);
        task.removeComment(commentId);
        return view(taskRepository.save(task));
    }

    @Transactional
    public TaskView uploadAttachment(Long id, AttachmentUpload upload) throws IOException {
        Task task = findTask(id);
        String storedPath = attachmentStorage.save(upload.bytes(), upload.originalName());
        task.addAttachment(new TaskAttachment(
                null,
                upload.originalName() == null ? "anexo" : upload.originalName(),
                upload.contentType(),
                upload.bytes().length,
                storedPath,
                null));
        return view(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public TaskAttachment findAttachment(Long taskId, Long attachmentId) {
        return findTask(taskId).findAttachment(attachmentId);
    }

    public byte[] loadAttachment(TaskAttachment attachment) throws IOException {
        return attachmentStorage.load(attachment.storedPath());
    }

    @Transactional
    public TaskView deleteAttachment(Long taskId, Long attachmentId) throws IOException {
        Task task = findTask(taskId);
        TaskAttachment attachment = task.findAttachment(attachmentId);
        task.removeAttachment(attachmentId);
        attachmentStorage.deleteIfExists(attachment.storedPath());
        return view(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public TaskSummary summary() {
        LocalDate today = LocalDate.now(clock);
        List<Task> openTasks = taskRepository.findAll().stream()
                .filter(task -> task.getStatus() != TaskStatus.CONCLUIDA)
                .toList();
        long overdue = openTasks.stream().filter(task -> task.effectiveDeadline().isBefore(today)).count();
        long todayCount = openTasks.stream().filter(task -> task.effectiveDeadline().isEqual(today)).count();
        long next7Days = openTasks.stream()
                .filter(task -> task.effectiveDeadline().isAfter(today) && !task.effectiveDeadline().isAfter(today.plusDays(7)))
                .count();
        return new TaskSummary(overdue, todayCount, next7Days);
    }

    private void apply(Task task, TaskCommand command) {
        Event event = null;
        if (command.eventId() != null) {
            event = eventRepository.findById(command.eventId())
                    .orElseThrow(() -> new NotFoundException("EVENT_NOT_FOUND", "Evento não encontrado"));
        }

        task.setEventId(event != null ? event.getId() : null);
        task.setTitulo(command.titulo().trim());
        task.setDescricao(blankToNull(command.descricao()));
        task.setPrazoData(command.prazoData());
        task.setPrazoDataFim(normalizeEndDate(command.prazoData(), command.prazoDataFim()));
        task.setAllDay(false);
        task.setPrazoHorario(requireStartTime(command.prazoHorario()));
        task.setPrazoHorarioFim(normalizeEndTime(command.prazoHorario(), command.prazoHorarioFim()));
        task.setStatus(command.status() == null || command.status().isBlank() ? TaskStatus.A_FAZER : parseStatus(command.status()));
        task.setTipoServico(resolveTipoServico(command.tipoServico(), event));
        task.replaceLabels(command.etiquetas());
        task.replaceChecklist(command.checklistItems());
    }

    private String resolveTipoServico(String tipoServico, Event event) {
        if (tipoServico != null && !tipoServico.isBlank()) {
            return tipoServico.trim();
        }
        return event != null && event.getTipoServico() != null ? event.getTipoServico().name() : null;
    }

    private LocalDate normalizeEndDate(LocalDate startDate, LocalDate endDate) {
        if (endDate == null || startDate == null || endDate.isEqual(startDate)) {
            return null;
        }
        return endDate.isBefore(startDate) ? startDate : endDate;
    }

    private LocalTime normalizeEndTime(LocalTime startTime, LocalTime endTime) {
        if (startTime == null) {
            return null;
        }
        return endTime == null ? startTime.plusHours(1) : endTime;
    }

    private LocalTime requireStartTime(LocalTime startTime) {
        if (startTime == null) {
            throw new BusinessException("TASK_START_TIME_REQUIRED", "O horário de início é obrigatório");
        }
        return startTime;
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TASK_NOT_FOUND", "Tarefa não encontrada"));
    }

    /** Monta as views buscando os eventos de uma vez, sem uma consulta por tarefa. */
    private List<TaskView> views(List<Task> tasks) {
        List<Long> eventIds = tasks.stream().map(Task::getEventId).filter(Objects::nonNull).distinct().toList();
        Map<Long, TaskEventSummary> events = eventIds.isEmpty() ? Map.of() : eventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(Event::getId, TaskEventSummary::from));
        return tasks.stream()
                .map(task -> new TaskView(task, task.getEventId() == null ? null : events.get(task.getEventId())))
                .toList();
    }

    private TaskView view(Task task) {
        return views(List.of(task)).get(0);
    }

    private TaskStatus parseStatus(String value) {
        try {
            return TaskStatus.valueOf(value.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new BusinessException("TASK_STATUS_INVALID", "Status de tarefa inválido: " + value);
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
