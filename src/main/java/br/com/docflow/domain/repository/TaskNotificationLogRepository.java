package br.com.docflow.domain.repository;

import br.com.docflow.domain.model.TaskReminderType;

import java.time.LocalDate;

/** Registro dos lembretes de prazo já enviados, para não repetir o mesmo aviso no mesmo dia. */
public interface TaskNotificationLogRepository {

    boolean wasSent(Long taskId, TaskReminderType reminderType, LocalDate sentDate);

    void record(Long taskId, TaskReminderType reminderType, LocalDate sentDate);
}
