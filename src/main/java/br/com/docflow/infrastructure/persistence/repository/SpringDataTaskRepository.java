package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.domain.model.TaskStatus;
import br.com.docflow.infrastructure.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SpringDataTaskRepository extends JpaRepository<TaskEntity, Long>, JpaSpecificationExecutor<TaskEntity> {
    List<TaskEntity> findByEventIdOrderByPrazoDataAscPrazoHorarioAscIdAsc(Long eventId);

    boolean existsByAutomationContractIdAndAutomationKey(Long automationContractId, String automationKey);

    List<TaskEntity> findByPrazoDataAndStatusNot(LocalDate prazoData, TaskStatus status);

    @Query("""
            select task from TaskEntity task
            where coalesce(task.prazoDataFim, task.prazoData) = :deadlineDate
              and task.status <> :status
            """)
    List<TaskEntity> findByEffectiveDeadlineAndStatusNot(
            @Param("deadlineDate") LocalDate deadlineDate,
            @Param("status") TaskStatus status
    );
}
