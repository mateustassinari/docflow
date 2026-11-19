package br.com.docflow.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record TaskDeadlineReminder(
        String title,
        String eventName,
        LocalDate deadlineDate,
        LocalTime deadlineTime,
        TaskReminderType reminderType
) {
}
