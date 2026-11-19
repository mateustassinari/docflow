package br.com.docflow.application.service;

import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.Task;
import br.com.docflow.domain.model.TaskDeadlineReminder;
import br.com.docflow.domain.model.TaskReminderType;
import br.com.docflow.domain.repository.EventRepository;
import br.com.docflow.domain.repository.TaskNotificationLogRepository;
import br.com.docflow.domain.repository.TaskRepository;
import br.com.docflow.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Envia, num único e-mail, os lembretes de tarefas abertas que vencem em 7 dias, em 3 dias e no
 * próprio dia. Cada lembrete é enviado uma vez por dia e por tipo.
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class TaskReminderService {

    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final TaskNotificationLogRepository logRepository;
    private final NotificationService notificationService;

    @Transactional
    public void sendDueReminders(LocalDate today) {
        Map<Long, Optional<Event>> eventsById = new HashMap<>();
        List<PendingReminder> pendingReminders = new ArrayList<>();
        collectForDate(today.plusDays(7), today, TaskReminderType.DAYS_7, eventsById, pendingReminders);
        collectForDate(today.plusDays(3), today, TaskReminderType.DAYS_3, eventsById, pendingReminders);
        collectForDate(today, today, TaskReminderType.DUE_DATE, eventsById, pendingReminders);

        if (pendingReminders.isEmpty()) {
            return;
        }

        notificationService.sendTaskDeadlineReminders(
                pendingReminders.stream()
                        .map(PendingReminder::reminder)
                        .toList()
        );

        for (PendingReminder pendingReminder : pendingReminders) {
            logRepository.record(pendingReminder.taskId(), pendingReminder.reminder().reminderType(), today);
        }
    }

    private void collectForDate(LocalDate deadlineDate, LocalDate sentDate, TaskReminderType reminderType,
                                Map<Long, Optional<Event>> eventsById, List<PendingReminder> pendingReminders) {
        for (Task task : taskRepository.findOpenByEffectiveDeadline(deadlineDate)) {
            if (logRepository.wasSent(task.getId(), reminderType, sentDate)) {
                continue;
            }

            pendingReminders.add(new PendingReminder(
                    task.getId(),
                    new TaskDeadlineReminder(
                            task.getTitulo(),
                            clientName(task, eventsById),
                            task.effectiveDeadline(),
                            task.getPrazoHorario(),
                            reminderType
                    )
            ));
        }
    }

    /** O e-mail mostra o nome do cliente do evento; cada evento é buscado uma vez por envio. */
    private String clientName(Task task, Map<Long, Optional<Event>> eventsById) {
        if (task.getEventId() == null) {
            return null;
        }
        return eventsById.computeIfAbsent(task.getEventId(), eventRepository::findById)
                .filter(event -> event.getClient() != null)
                .map(event -> event.getClient().getNome())
                .orElse(null);
    }

    private record PendingReminder(Long taskId, TaskDeadlineReminder reminder) {
    }
}
