package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.domain.model.TaskReminderType;
import br.com.docflow.infrastructure.persistence.entity.TaskEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(TaskNotificationLogRepositoryImpl.class)
class TaskNotificationLogRepositoryImplIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private TaskNotificationLogRepositoryImpl repository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldRecordReminderPerTaskTypeAndDay() {
        TaskEntity task = new TaskEntity();
        task.setTitulo("Tarefa");
        task.setPrazoData(LocalDate.of(2026, 11, 20));
        task.setPrazoHorario(LocalTime.of(9, 0));
        entityManager.persistAndFlush(task);
        LocalDate today = LocalDate.of(2026, 11, 17);

        repository.record(task.getId(), TaskReminderType.DAYS_3, today);
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.wasSent(task.getId(), TaskReminderType.DAYS_3, today)).isTrue();
        assertThat(repository.wasSent(task.getId(), TaskReminderType.DUE_DATE, today))
                .as("outro tipo de lembrete no mesmo dia ainda não foi enviado").isFalse();
        assertThat(repository.wasSent(task.getId(), TaskReminderType.DAYS_3, today.plusDays(1))).isFalse();
    }
}
