package br.com.docflow.testsupport;

import br.com.docflow.domain.model.ChecklistItem;
import br.com.docflow.domain.model.Task;
import br.com.docflow.domain.model.TaskAttachment;
import br.com.docflow.domain.model.TaskComment;
import br.com.docflow.domain.model.TaskFilter;
import br.com.docflow.domain.model.TaskStatus;
import br.com.docflow.domain.repository.TaskRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TaskRepository em memória para testes de aplicação. Guarda cópias (como um banco faria) e
 * gera ids para a tarefa e para os filhos novos. A busca segue as mesmas regras do adaptador JPA,
 * que tem seu próprio teste com Postgres (TaskRepositoryImplIT).
 */
public class InMemoryTaskRepository implements TaskRepository {

    private final Map<Long, Task> tasks = new LinkedHashMap<>();
    private final AtomicLong ids = new AtomicLong(1);

    @Override
    public Task save(Task task) {
        Task copy = copy(task);
        if (copy.getId() == null) {
            copy.setId(ids.getAndIncrement());
        }
        copy.setChecklist(new ArrayList<>(copy.getChecklist().stream()
                .map(item -> item.id() != null ? item : new ChecklistItem(ids.getAndIncrement(), item.texto(), item.concluido(), item.ordem()))
                .toList()));
        copy.setComments(new ArrayList<>(copy.getComments().stream()
                .map(comment -> comment.id() != null ? comment : new TaskComment(ids.getAndIncrement(), comment.texto(), null, null))
                .toList()));
        copy.setAttachments(new ArrayList<>(copy.getAttachments().stream()
                .map(a -> a.id() != null ? a : new TaskAttachment(ids.getAndIncrement(), a.originalName(), a.contentType(), a.sizeBytes(), a.storedPath(), null))
                .toList()));
        tasks.put(copy.getId(), copy);
        return copy(copy);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(tasks.get(id)).map(this::copy);
    }

    @Override
    public void deleteById(Long id) {
        tasks.remove(id);
    }

    @Override
    public List<Task> findAll() {
        return tasks.values().stream().map(this::copy).toList();
    }

    @Override
    public List<Task> findByEventId(Long eventId) {
        return sorted(tasks.values().stream().filter(task -> Objects.equals(eventId, task.getEventId())).toList());
    }

    @Override
    public boolean existsByAutomation(Long contractId, String automationKey) {
        return tasks.values().stream().anyMatch(task -> task.getAutomation() != null
                && Objects.equals(contractId, task.getAutomation().contractId())
                && Objects.equals(automationKey, task.getAutomation().key()));
    }

    @Override
    public List<Task> findOpenByEffectiveDeadline(LocalDate deadline) {
        return tasks.values().stream()
                .filter(task -> task.getStatus() != TaskStatus.CONCLUIDA && deadline.equals(task.effectiveDeadline()))
                .map(this::copy)
                .toList();
    }

    @Override
    public List<Task> search(TaskFilter filter) {
        return sorted(tasks.values().stream()
                .filter(task -> filter.start() == null || !task.effectiveDeadline().isBefore(filter.start()))
                .filter(task -> filter.end() == null || !task.getPrazoData().isAfter(filter.end()))
                .filter(task -> filter.status() == null || filter.status() == task.getStatus())
                .filter(task -> filter.tipoServico() == null || filter.tipoServico().isBlank() || filter.tipoServico().equals(task.getTipoServico()))
                .filter(task -> filter.eventId() == null || filter.eventId().equals(task.getEventId()))
                .filter(task -> filter.label() == null || filter.label().isBlank() || task.getEtiquetas().stream()
                        .anyMatch(label -> label.toLowerCase().contains(filter.label().trim().toLowerCase())))
                .toList());
    }

    public List<Task> all() {
        return findAll();
    }

    private List<Task> sorted(List<Task> list) {
        return list.stream()
                .sorted(Comparator.comparing(Task::getPrazoData)
                        .thenComparing(task -> task.getPrazoHorario() == null ? LocalTime.MIN : task.getPrazoHorario())
                        .thenComparing(Task::getId))
                .map(this::copy)
                .toList();
    }

    private Task copy(Task source) {
        Task task = new Task();
        task.setId(source.getId());
        task.setEventId(source.getEventId());
        task.setTitulo(source.getTitulo());
        task.setDescricao(source.getDescricao());
        task.setPrazoData(source.getPrazoData());
        task.setPrazoDataFim(source.getPrazoDataFim());
        task.setPrazoHorario(source.getPrazoHorario());
        task.setPrazoHorarioFim(source.getPrazoHorarioFim());
        task.setAllDay(source.isAllDay());
        task.setStatus(source.getStatus());
        task.setTipoServico(source.getTipoServico());
        task.setAutomation(source.getAutomation());
        task.setChecklist(new ArrayList<>(source.getChecklist()));
        task.setEtiquetas(new ArrayList<>(source.getEtiquetas()));
        task.setComments(new ArrayList<>(source.getComments()));
        task.setAttachments(new ArrayList<>(source.getAttachments()));
        task.setCreatedAt(source.getCreatedAt());
        task.setUpdatedAt(source.getUpdatedAt());
        return task;
    }
}
