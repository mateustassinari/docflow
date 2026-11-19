package br.com.docflow.application.readmodel;

import br.com.docflow.domain.model.Event;

import java.time.LocalDate;

/** Resumo do evento mostrado junto com a tarefa. */
public record TaskEventSummary(Long id, String clienteNome, String tipoEvento, String tipoServico, LocalDate data) {

    public static TaskEventSummary from(Event event) {
        return new TaskEventSummary(
                event.getId(),
                event.getClient() != null ? event.getClient().getNome() : null,
                event.getTipoEvento(),
                event.getTipoServico() != null ? event.getTipoServico().name() : null,
                event.getData());
    }
}
