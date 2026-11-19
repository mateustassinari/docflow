package br.com.docflow.application.readmodel;

/** Contagem de tarefas abertas atrasadas, para hoje e para os próximos 7 dias. */
public record TaskSummary(long overdue, long today, long next7Days) {
}
