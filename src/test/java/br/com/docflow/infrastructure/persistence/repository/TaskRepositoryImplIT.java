package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.domain.model.ChecklistItem;
import br.com.docflow.domain.model.Task;
import br.com.docflow.domain.model.TaskAttachment;
import br.com.docflow.domain.model.TaskAutomation;
import br.com.docflow.domain.model.TaskComment;
import br.com.docflow.domain.model.TaskFilter;
import br.com.docflow.domain.model.TaskStatus;
import br.com.docflow.infrastructure.persistence.entity.ClientEntity;
import br.com.docflow.infrastructure.persistence.entity.EventEntity;
import br.com.docflow.infrastructure.persistence.mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Persistência real (Postgres + migrations Flyway). Prova que o save por merge não apaga
 * comentários e anexos, que são coleções com orphanRemoval.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import({TaskRepositoryImpl.class, TaskMapper.class})
class TaskRepositoryImplIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private TaskRepositoryImpl repository;
    @Autowired
    private TestEntityManager entityManager;

    private Long eventId;

    @BeforeEach
    void setUp() {
        ClientEntity client = new ClientEntity();
        client.setNome("Cliente Teste");
        client.setCpfCnpj("000.000.000-" + System.nanoTime() % 100);
        client.setEmail("cliente@exemplo.com");
        client.setTelefone("11999999999");
        entityManager.persist(client);

        EventEntity event = new EventEntity();
        event.setClient(client);
        event.setTipoEvento("Casamento");
        event.setData(LocalDate.of(2026, 12, 20));
        event.setLocal("Local");
        event.setValor(new BigDecimal("1000"));
        event.setTipoServico("PINTURA_AO_VIVO");
        entityManager.persist(event);
        eventId = event.getId();
        entityManager.flush();
    }

    @Test
    void savingChangedTaskShouldKeepCommentsAttachmentsAndLabels() {
        Task created = repository.save(taskWithChildren());
        Task loaded = reload(created.getId());

        loaded.setTitulo("Título alterado");
        loaded.replaceChecklist(List.of(new ChecklistItem(null, "Item novo", true, null)));
        repository.save(loaded);
        Task reloaded = reload(created.getId());

        assertThat(reloaded.getTitulo()).isEqualTo("Título alterado");
        assertThat(reloaded.getChecklist()).extracting(ChecklistItem::texto).containsExactly("Item novo");
        assertThat(reloaded.getComments())
                .as("comentários não fazem parte da edição e não podem ser apagados nem recriados")
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(loaded.getComments());
        assertThat(reloaded.getAttachments())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(loaded.getAttachments());
        assertThat(reloaded.getEtiquetas()).containsExactly("urgente", "cliente");
    }

    @Test
    void removingOneCommentShouldDeleteOnlyThatComment() {
        Task loaded = reload(repository.save(taskWithChildren()).getId());
        Long removedId = loaded.getComments().get(0).id();
        Long keptId = loaded.getComments().get(1).id();

        loaded.removeComment(removedId);
        repository.save(loaded);

        assertThat(reload(loaded.getId()).getComments()).extracting(TaskComment::id).containsExactly(keptId);
    }

    @Test
    void newCommentShouldComeBackWithIdAndKeepExistingOnes() {
        Task loaded = reload(repository.save(taskWithChildren()).getId());

        loaded.addComment("terceiro");
        Task saved = repository.save(loaded);

        assertThat(saved.getComments()).hasSize(3).allSatisfy(comment -> assertThat(comment.id()).isNotNull());
        assertThat(saved.getComments().get(2).texto()).isEqualTo("terceiro");
    }

    @Test
    void searchShouldApplyEachFilter() {
        Task early = repository.save(task("Cedo", LocalDate.of(2026, 11, 1), null, TaskStatus.A_FAZER, List.of("Urgente")));
        Task period = repository.save(task("Período", LocalDate.of(2026, 10, 25), LocalDate.of(2026, 11, 5), TaskStatus.CONCLUIDA, List.of()));
        Task late = repository.save(task("Tarde", LocalDate.of(2026, 12, 1), null, TaskStatus.A_FAZER, List.of("outra")));
        entityManager.clear();

        assertThat(ids(repository.search(new TaskFilter(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), null, null, null, null))))
                .as("start compara com o prazo efetivo: o período que termina em 05/11 entra")
                .containsExactly(period.getId(), early.getId());
        assertThat(ids(repository.search(new TaskFilter(null, null, TaskStatus.CONCLUIDA, null, null, null))))
                .containsExactly(period.getId());
        assertThat(ids(repository.search(new TaskFilter(null, null, null, null, null, "URG"))))
                .as("etiqueta: busca parcial sem diferenciar maiúsculas")
                .containsExactly(early.getId());
        assertThat(ids(repository.search(new TaskFilter(null, null, null, "PINTURA_AO_VIVO", eventId, null))))
                .containsExactly(period.getId(), early.getId(), late.getId());
    }

    @Test
    void shouldFindOpenTasksByEffectiveDeadlineAndAutomationKey() {
        Task open = task("Aberta", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 20), TaskStatus.A_FAZER, List.of());
        open.setAutomation(new TaskAutomation("CONTRACT_SIGNED", 99L, "PINTURA_HOSPEDAGEM"));
        Task savedOpen = repository.save(open);
        repository.save(task("Concluída", LocalDate.of(2026, 11, 20), null, TaskStatus.CONCLUIDA, List.of()));
        entityManager.clear();

        assertThat(ids(repository.findOpenByEffectiveDeadline(LocalDate.of(2026, 11, 20)))).containsExactly(savedOpen.getId());
        assertThat(repository.existsByAutomation(99L, "PINTURA_HOSPEDAGEM")).isTrue();
        assertThat(repository.existsByAutomation(99L, "PINTURA_ENVIAR")).isFalse();
        assertThat(repository.findById(savedOpen.getId()).orElseThrow().getAutomation())
                .isEqualTo(new TaskAutomation("CONTRACT_SIGNED", 99L, "PINTURA_HOSPEDAGEM"));
    }

    private Task reload(Long id) {
        entityManager.flush();
        entityManager.clear();
        return repository.findById(id).orElseThrow();
    }

    private Task taskWithChildren() {
        Task task = task("Compra de material", LocalDate.of(2026, 12, 5), null, TaskStatus.A_FAZER, List.of("urgente", "cliente"));
        task.replaceChecklist(List.of(new ChecklistItem(null, "Tela", false, null)));
        task.addComment("primeiro");
        task.addComment("segundo");
        task.addAttachment(new TaskAttachment(null, "orcamento.pdf", "application/pdf", 123L, "/tmp/orcamento.pdf", null));
        return task;
    }

    private Task task(String titulo, LocalDate start, LocalDate end, TaskStatus status, List<String> labels) {
        Task task = new Task();
        task.setEventId(eventId);
        task.setTitulo(titulo);
        task.setPrazoData(start);
        task.setPrazoDataFim(end);
        task.setPrazoHorario(LocalTime.of(9, 0));
        task.setPrazoHorarioFim(LocalTime.of(10, 0));
        task.setStatus(status);
        task.setTipoServico("PINTURA_AO_VIVO");
        task.replaceLabels(new ArrayList<>(labels));
        return task;
    }

    private List<Long> ids(List<Task> tasks) {
        return tasks.stream().map(Task::getId).toList();
    }
}
