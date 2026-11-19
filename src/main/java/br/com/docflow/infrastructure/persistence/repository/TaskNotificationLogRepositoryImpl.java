package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.domain.model.TaskReminderType;
import br.com.docflow.domain.repository.TaskNotificationLogRepository;
import br.com.docflow.infrastructure.persistence.entity.TaskNotificationLogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@SuppressWarnings("null")
@Component
@RequiredArgsConstructor
public class TaskNotificationLogRepositoryImpl implements TaskNotificationLogRepository {

    private final SpringDataTaskNotificationLogRepository springDataLogRepository;
    private final SpringDataTaskRepository springDataTaskRepository;

    @Override
    public boolean wasSent(Long taskId, TaskReminderType reminderType, LocalDate sentDate) {
        return springDataLogRepository.existsByTaskIdAndReminderTypeAndSentDate(taskId, reminderType, sentDate);
    }

    @Override
    public void record(Long taskId, TaskReminderType reminderType, LocalDate sentDate) {
        TaskNotificationLogEntity log = new TaskNotificationLogEntity();
        log.setTask(springDataTaskRepository.getReferenceById(taskId));
        log.setReminderType(reminderType);
        log.setSentDate(sentDate);
        springDataLogRepository.save(log);
    }
}
