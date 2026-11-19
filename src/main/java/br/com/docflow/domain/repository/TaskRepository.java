package br.com.docflow.domain.repository;

import br.com.docflow.domain.model.Task;
import br.com.docflow.domain.model.TaskFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    /**
     * Cria ou atualiza a tarefa. Comentários e anexos são conciliados por id: os existentes
     * são mantidos, os sem id são criados e os ausentes são removidos.
     */
    Task save(Task task);

    Optional<Task> findById(Long id);

    void deleteById(Long id);

    List<Task> findAll();

    /** Tarefas do evento, por prazo, horário e id. */
    List<Task> findByEventId(Long eventId);

    boolean existsByAutomation(Long contractId, String automationKey);

    /** Tarefas não concluídas cujo prazo efetivo (fim ou início) é a data informada. */
    List<Task> findOpenByEffectiveDeadline(LocalDate deadline);

    /** Tarefas que atendem ao filtro, por prazo, horário (sem horário primeiro) e id. */
    List<Task> search(TaskFilter filter);
}
