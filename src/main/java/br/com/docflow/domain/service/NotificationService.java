package br.com.docflow.domain.service;

import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.TaskDeadlineReminder;

import java.util.List;

public interface NotificationService {
    void sendContractSignedAlert(Contract contract);

    void sendTaskDeadlineReminders(List<TaskDeadlineReminder> reminders);
}
