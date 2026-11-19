package br.com.docflow.domain.model;

import java.time.LocalDateTime;

/** Comentário de uma tarefa. Datas são preenchidas pela persistência. */
public record TaskComment(Long id, String texto, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
