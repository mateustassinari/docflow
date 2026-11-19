package br.com.docflow.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import br.com.docflow.application.usecase.command.TaskCommand;
import br.com.docflow.domain.model.ChecklistItem;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TaskRequest {
    private Long eventId;

    @NotBlank(message = "O título da tarefa é obrigatório")
    private String titulo;

    private String descricao;

    @NotNull(message = "O prazo da tarefa é obrigatório")
    private LocalDate prazoData;

    private LocalDate prazoDataFim;
    @NotNull(message = "O horário de início é obrigatório")
    private LocalTime prazoHorario;
    private LocalTime prazoHorarioFim;
    private String status;
    private String tipoServico;
    private List<String> etiquetas = new ArrayList<>();
    private List<TaskChecklistItemRequest> checklistItems = new ArrayList<>();

    public TaskCommand toCommand() {
        List<ChecklistItem> items = checklistItems == null ? null : checklistItems.stream()
                .map(item -> new ChecklistItem(item.getId(), item.getTexto(), item.isConcluido(), item.getOrdem()))
                .toList();
        return new TaskCommand(eventId, titulo, descricao, prazoData, prazoDataFim, prazoHorario, prazoHorarioFim,
                status, tipoServico, etiquetas, items);
    }
}
