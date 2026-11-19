package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.domain.model.TaskReminderType;
import br.com.docflow.infrastructure.persistence.entity.TaskNotificationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface SpringDataTaskNotificationLogRepository extends JpaRepository<TaskNotificationLogEntity, Long> {
    boolean existsByTaskIdAndReminderTypeAndSentDate(Long taskId, TaskReminderType reminderType, LocalDate sentDate);
}
