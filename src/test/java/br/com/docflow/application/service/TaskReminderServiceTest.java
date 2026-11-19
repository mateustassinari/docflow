package br.com.docflow.application.service;

import br.com.docflow.domain.model.Client;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.Task;
import br.com.docflow.domain.model.TaskDeadlineReminder;
import br.com.docflow.domain.model.TaskReminderType;
import br.com.docflow.domain.model.TaskStatus;
import br.com.docflow.domain.service.NotificationService;
import br.com.docflow.testsupport.InMemoryEventRepository;
import br.com.docflow.testsupport.InMemoryTaskNotificationLogRepository;
import br.com.docflow.testsupport.InMemoryTaskNotificationLogRepository.Entry;
import br.com.docflow.testsupport.InMemoryTaskRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TaskReminderServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);

    private final InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
    private final InMemoryEventRepository eventRepository = new InMemoryEventRepository();
    private final InMemoryTaskNotificationLogRepository logRepository = new InMemoryTaskNotificationLogRepository();
    private final NotificationService notificationService = mock(NotificationService.class);
    private final TaskReminderService service =
            new TaskReminderService(taskRepository, eventRepository, logRepository, notificationService);

    @Test
    void shouldRemindSevenDaysThreeDaysAndOnTheDueDateInOneEmail() {
        Task in7 = givenTask("Em 7 dias", TODAY.plusDays(7), null, TaskStatus.A_FAZER, null);
        Task in3 = givenTask("Em 3 dias", TODAY.plusDays(3), null, TaskStatus.EM_ANDAMENTO, null);
        Task today = givenTask("Hoje", TODAY, null, TaskStatus.A_FAZER, null);
        givenTask("Em 5 dias", TODAY.plusDays(5), null, TaskStatus.A_FAZER, null);

        service.sendDueReminders(TODAY);

        assertThat(sentReminders()).extracting(TaskDeadlineReminder::title, TaskDeadlineReminder::reminderType)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Em 7 dias", TaskReminderType.DAYS_7),
                        org.assertj.core.groups.Tuple.tuple("Em 3 dias", TaskReminderType.DAYS_3),
                        org.assertj.core.groups.Tuple.tuple("Hoje", TaskReminderType.DUE_DATE));
        assertThat(logRepository.entries()).containsExactly(
                new Entry(in7.getId(), TaskReminderType.DAYS_7, TODAY),
                new Entry(in3.getId(), TaskReminderType.DAYS_3, TODAY),
                new Entry(today.getId(), TaskReminderType.DUE_DATE, TODAY));
    }

    @Test
    void periodTaskShouldBeRemindedByItsEndDate() {
        givenTask("Produção", TODAY.minusDays(2), TODAY.plusDays(3), TaskStatus.A_FAZER, null);

        service.sendDueReminders(TODAY);

        assertThat(sentReminders()).singleElement().satisfies(reminder -> {
            assertThat(reminder.reminderType()).isEqualTo(TaskReminderType.DAYS_3);
            assertThat(reminder.deadlineDate())
                    .as("o lembrete mostra o prazo efetivo (fim do período)")
                    .isEqualTo(TODAY.plusDays(3));
        });
    }

    @Test
    void reminderShouldCarryClientNameAndStartTime() {
        Event event = givenEvent("Isabella");
        givenTask("Compra de material", TODAY, null, TaskStatus.A_FAZER, event.getId());
        givenTask("Sem evento", TODAY, null, TaskStatus.A_FAZER, null);

        service.sendDueReminders(TODAY);

        assertThat(sentReminders()).extracting(TaskDeadlineReminder::eventName).containsExactly("Isabella", null);
        assertThat(sentReminders()).extracting(TaskDeadlineReminder::deadlineTime)
                .containsOnly(LocalTime.of(9, 0));
    }

    @Test
    void completedTasksShouldNotBeReminded() {
        givenTask("Concluída", TODAY, null, TaskStatus.CONCLUIDA, null);

        service.sendDueReminders(TODAY);

        verify(notificationService, never()).sendTaskDeadlineReminders(any());
        assertThat(logRepository.entries()).isEmpty();
    }

    @Test
    void sameReminderShouldNotBeSentTwiceOnTheSameDay() {
        givenTask("Hoje", TODAY, null, TaskStatus.A_FAZER, null);

        service.sendDueReminders(TODAY);
        service.sendDueReminders(TODAY);

        verify(notificationService, times(1)).sendTaskDeadlineReminders(any());
        assertThat(logRepository.entries()).hasSize(1);
    }

    @Test
    void nothingDueShouldSendNothing() {
        service.sendDueReminders(TODAY);

        verify(notificationService, never()).sendTaskDeadlineReminders(any());
    }

    @SuppressWarnings("unchecked")
    private List<TaskDeadlineReminder> sentReminders() {
        ArgumentCaptor<List<TaskDeadlineReminder>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).sendTaskDeadlineReminders(captor.capture());
        return captor.getValue();
    }

    private Event givenEvent(String clientName) {
        Client client = new Client();
        client.setNome(clientName);
        Event event = new Event();
        event.setClient(client);
        return eventRepository.given(event);
    }

    private Task givenTask(String title, LocalDate start, LocalDate end, TaskStatus status, Long eventId) {
        Task task = new Task();
        task.setTitulo(title);
        task.setPrazoData(start);
        task.setPrazoDataFim(end);
        task.setPrazoHorario(LocalTime.of(9, 0));
        task.setStatus(status);
        task.setEventId(eventId);
        return taskRepository.save(task);
    }
}
