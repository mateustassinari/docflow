package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.Task;
import br.com.docflow.domain.model.TaskFilter;
import br.com.docflow.domain.model.TaskStatus;
import br.com.docflow.domain.repository.TaskRepository;
import br.com.docflow.infrastructure.persistence.entity.EventEntity;
import br.com.docflow.infrastructure.persistence.entity.TaskEntity;
import br.com.docflow.infrastructure.persistence.entity.TaskLabelEntity;
import br.com.docflow.infrastructure.persistence.mapper.TaskMapper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("null")
@Component
@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepository {

    private final SpringDataTaskRepository springDataTaskRepository;
    private final SpringDataEventRepository springDataEventRepository;
    private final TaskMapper taskMapper;

    /**
     * Atualiza a entidade já carregada em vez de criar outra a partir do domínio: assim o
     * merge das coleções filhas (com orphanRemoval) não apaga comentários e anexos.
     * saveAndFlush faz os filhos novos voltarem com id.
     */
    @Override
    @Transactional
    public Task save(Task task) {
        TaskEntity entity = task.getId() == null
                ? new TaskEntity()
                : springDataTaskRepository.findById(task.getId())
                        .orElseThrow(() -> new NotFoundException("TASK_NOT_FOUND", "Tarefa não encontrada"));
        EventEntity event = task.getEventId() == null ? null : springDataEventRepository.getReferenceById(task.getEventId());
        taskMapper.mergeInto(task, entity, event);
        return taskMapper.toDomain(springDataTaskRepository.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Task> findById(Long id) {
        return springDataTaskRepository.findById(id).map(taskMapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        springDataTaskRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return springDataTaskRepository.findAll().stream().map(taskMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findByEventId(Long eventId) {
        return springDataTaskRepository.findByEventIdOrderByPrazoDataAscPrazoHorarioAscIdAsc(eventId).stream()
                .map(taskMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAutomation(Long contractId, String automationKey) {
        return springDataTaskRepository.existsByAutomationContractIdAndAutomationKey(contractId, automationKey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findOpenByEffectiveDeadline(LocalDate deadline) {
        return springDataTaskRepository.findByEffectiveDeadlineAndStatusNot(deadline, TaskStatus.CONCLUIDA).stream()
                .map(taskMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> search(TaskFilter filter) {
        return springDataTaskRepository.findAll(toSpecification(filter)).stream()
                .sorted(Comparator
                        .comparing(TaskEntity::getPrazoData)
                        .thenComparing(task -> task.getPrazoHorario() == null ? LocalTime.MIN : task.getPrazoHorario())
                        .thenComparing(TaskEntity::getId))
                .map(taskMapper::toDomain)
                .toList();
    }

    private Specification<TaskEntity> toSpecification(TaskFilter filter) {
        Specification<TaskEntity> spec = Specification.where(null);

        if (filter.start() != null) {
            LocalDate start = filter.start();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(cb.coalesce(root.get("prazoDataFim"), root.get("prazoData")), start));
        }
        if (filter.end() != null) {
            LocalDate end = filter.end();
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("prazoData"), end));
        }
        if (filter.status() != null) {
            TaskStatus status = filter.status();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (filter.tipoServico() != null && !filter.tipoServico().isBlank()) {
            String tipoServico = filter.tipoServico();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipoServico"), tipoServico));
        }
        if (filter.eventId() != null) {
            Long eventId = filter.eventId();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("event").get("id"), eventId));
        }
        if (filter.label() != null && !filter.label().isBlank()) {
            String normalizedLabel = filter.label().trim().toLowerCase();
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Join<TaskEntity, TaskLabelEntity> labels = root.join("labels", JoinType.INNER);
                return cb.like(cb.lower(labels.get("label")), "%" + normalizedLabel + "%");
            });
        }
        return spec;
    }
}
