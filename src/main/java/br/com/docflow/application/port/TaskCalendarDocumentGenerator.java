package br.com.docflow.application.port;

import br.com.docflow.application.readmodel.TaskView;

import java.time.LocalDate;
import java.util.List;

/** Gera o documento (PDF) do calendário de tarefas na visão de mês, semana ou dia. */
public interface TaskCalendarDocumentGenerator {

    byte[] generate(List<TaskView> tasks, String view, LocalDate date);
}
