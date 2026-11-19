package br.com.docflow.application.usecase.command;

import br.com.docflow.domain.model.ChecklistItem;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Dados de criação ou edição de uma tarefa. Status e tipo de serviço chegam como texto livre. */
public record TaskCommand(
        Long eventId,
        String titulo,
        String descricao,
        LocalDate prazoData,
        LocalDate prazoDataFim,
        LocalTime prazoHorario,
        LocalTime prazoHorarioFim,
        String status,
        String tipoServico,
        List<String> etiquetas,
        List<ChecklistItem> checklistItems
) {
}
