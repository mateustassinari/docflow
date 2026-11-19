package br.com.docflow.application.usecase.command;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateTaskDeadlineCommand(
        LocalDate prazoData,
        LocalDate prazoDataFim,
        LocalTime prazoHorario,
        LocalTime prazoHorarioFim
) {
}
