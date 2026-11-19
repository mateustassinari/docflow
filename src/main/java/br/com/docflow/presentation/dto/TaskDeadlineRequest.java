package br.com.docflow.presentation.dto;

import jakarta.validation.constraints.NotNull;
import br.com.docflow.application.usecase.command.UpdateTaskDeadlineCommand;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TaskDeadlineRequest {
    @NotNull(message = "A data do prazo é obrigatória")
    private LocalDate prazoData;

    private LocalDate prazoDataFim;
    @NotNull(message = "O horário de início é obrigatório")
    private LocalTime prazoHorario;
    private LocalTime prazoHorarioFim;

    public UpdateTaskDeadlineCommand toCommand() {
        return new UpdateTaskDeadlineCommand(prazoData, prazoDataFim, prazoHorario, prazoHorarioFim);
    }
}
