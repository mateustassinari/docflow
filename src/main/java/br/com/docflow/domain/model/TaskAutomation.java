package br.com.docflow.domain.model;

/**
 * Origem de uma tarefa criada automaticamente. A dupla (contractId, key) identifica a tarefa
 * e garante que a automação não a crie duas vezes.
 */
public record TaskAutomation(String source, Long contractId, String key) {
}
