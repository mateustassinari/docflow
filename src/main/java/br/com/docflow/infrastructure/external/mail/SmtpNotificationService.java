package br.com.docflow.infrastructure.external.mail;

import br.com.docflow.domain.model.BusinessProfile;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.TaskDeadlineReminder;
import br.com.docflow.domain.model.TaskReminderType;
import br.com.docflow.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class SmtpNotificationService implements NotificationService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final BusinessProfile businessProfile;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.notifications.email.enabled:false}")
    private boolean emailNotificationsEnabled;

    @Value("${app.notifications.email.to:}")
    private String notificationRecipient;

    @Override
    public void sendContractSignedAlert(Contract contract) {
        sendMessage(
                "Contrato assinado: " + contract.getEvent().getClient().getNome(),
                buildContractSignedText(contract)
        );
    }

    @Override
    public void sendTaskDeadlineReminders(List<TaskDeadlineReminder> reminders) {
        if (reminders == null || reminders.isEmpty()) {
            return;
        }

        String subject = reminders.size() == 1
                ? "1 tarefa pendente com prazo próximo"
                : reminders.size() + " tarefas pendentes com prazo próximo";

        sendMessage(subject, buildTaskDeadlineRemindersText(reminders));
    }

    private void sendMessage(String subject, String text) {
        if (!emailNotificationsEnabled) {
            System.out.println("E-mail de notificação ignorado: notificações por e-mail desativadas.");
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            System.out.println("E-mail de notificação ignorado: SMTP não configurado.");
            return;
        }

        if (fromEmail == null || fromEmail.isBlank()) {
            System.out.println("E-mail de notificação ignorado: remetente SMTP não configurado.");
            return;
        }

        if (notificationRecipient == null || notificationRecipient.isBlank()) {
            System.out.println("E-mail de notificação ignorado: destinatário não configurado (app.notifications.email.to).");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(notificationRecipient);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Falha ao enviar e-mail de notificação: " + e.getMessage());
        }
    }

    private String buildContractSignedText(Contract contract) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
        String valorFormatado = contract.getEvent().getValor() != null
                ? format.format(contract.getEvent().getValor())
                : "Não informado";

        return String.format(
                businessProfile.greeting() + "\n\n"
                        + "O cliente %s acabou de assinar o contrato digital no Autentique.\n\n"
                        + "Detalhes do evento:\n"
                        + "- Tipo: %s\n"
                        + "- Valor: %s\n\n"
                        + "Parabéns por mais um evento fechado!",
                contract.getEvent().getClient().getNome(),
                contract.getEvent().getTipoEvento(),
                valorFormatado
        );
    }

    private String buildTaskDeadlineRemindersText(List<TaskDeadlineReminder> reminders) {
        Map<TaskReminderType, List<TaskDeadlineReminder>> byType = reminders.stream()
                .collect(Collectors.groupingBy(TaskDeadlineReminder::reminderType));

        StringBuilder text = new StringBuilder();
        text.append(businessProfile.greeting()).append("\n\n");
        text.append("Estas tarefas ainda não foram concluídas e precisam de atenção:\n");

        appendReminderSection(text, "Vencem hoje", byType.getOrDefault(TaskReminderType.DUE_DATE, List.of()));
        appendReminderSection(text, "Vencem em 3 dias", byType.getOrDefault(TaskReminderType.DAYS_3, List.of()));
        appendReminderSection(text, "Vencem em 7 dias", byType.getOrDefault(TaskReminderType.DAYS_7, List.of()));

        text.append("\nAcesse o sistema para revisar ou concluir as tarefas.");
        return text.toString();
    }

    private void appendReminderSection(StringBuilder text, String title, List<TaskDeadlineReminder> reminders) {
        if (reminders == null || reminders.isEmpty()) {
            return;
        }

        text.append("\n").append(title).append(":\n");
        for (TaskDeadlineReminder reminder : reminders) {
            text.append("- ")
                    .append(reminder.title())
                    .append(" | Evento: ")
                    .append(reminder.eventName() == null || reminder.eventName().isBlank() ? "Tarefa avulsa" : reminder.eventName())
                    .append(" | Prazo: ")
                    .append(formatDeadline(reminder))
                    .append("\n");
        }
    }

    private String formatDeadline(TaskDeadlineReminder reminder) {
        String deadline = reminder.deadlineDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        if (reminder.deadlineTime() != null) {
            deadline += " às " + reminder.deadlineTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return deadline;
    }
}
