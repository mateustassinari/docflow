package br.com.docflow.infrastructure.cron;

import br.com.docflow.application.service.TaskReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class TaskReminderScheduler {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    private final TaskReminderService taskReminderService;

    @Scheduled(cron = "${app.tasks.reminders.cron:0 0 8 * * *}", zone = "America/Sao_Paulo")
    public void sendTaskDeadlineReminders() {
        taskReminderService.sendDueReminders(LocalDate.now(SAO_PAULO));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void sendTaskDeadlineRemindersOnStartup() {
        taskReminderService.sendDueReminders(LocalDate.now(SAO_PAULO));
    }
}
