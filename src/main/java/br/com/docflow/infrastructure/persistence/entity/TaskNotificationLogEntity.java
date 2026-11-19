package br.com.docflow.infrastructure.persistence.entity;

import br.com.docflow.domain.model.TaskReminderType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "task_notification_logs")
public class TaskNotificationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private TaskEntity task;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false)
    private TaskReminderType reminderType;

    @Column(name = "sent_date", nullable = false)
    private LocalDate sentDate;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    void prePersist() {
        sentAt = LocalDateTime.now();
    }
}
