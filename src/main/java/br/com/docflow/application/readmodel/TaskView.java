package br.com.docflow.application.readmodel;

import br.com.docflow.domain.model.Task;

/** Tarefa pronta para exibição: o agregado mais o resumo do evento (null se não houver evento). */
public record TaskView(Task task, TaskEventSummary event) {
}
