package br.com.docflow.domain.model;

import java.time.LocalDate;

/**
 * Filtro da listagem de tarefas. Campos nulos (ou em branco) não filtram.
 * start compara com o prazo efetivo (fim, se houver; senão início); end compara com o início.
 */
public record TaskFilter(
        LocalDate start,
        LocalDate end,
        TaskStatus status,
        String tipoServico,
        Long eventId,
        String label
) {
    public static TaskFilter none() {
        return new TaskFilter(null, null, null, null, null, null);
    }
}
