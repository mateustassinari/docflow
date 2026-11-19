package br.com.docflow.testsupport;

import br.com.docflow.domain.model.TaskReminderType;
import br.com.docflow.domain.repository.TaskNotificationLogRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InMemoryTaskNotificationLogRepository implements TaskNotificationLogRepository {

    public record Entry(Long taskId, TaskReminderType reminderType, LocalDate sentDate) {
    }

    private final List<Entry> entries = new ArrayList<>();

    @Override
    public boolean wasSent(Long taskId, TaskReminderType reminderType, LocalDate sentDate) {
        return entries.contains(new Entry(taskId, reminderType, sentDate));
    }

    @Override
    public void record(Long taskId, TaskReminderType reminderType, LocalDate sentDate) {
        entries.add(new Entry(taskId, reminderType, sentDate));
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }
}
