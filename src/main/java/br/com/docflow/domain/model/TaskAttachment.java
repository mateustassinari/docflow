package br.com.docflow.domain.model;

import java.time.LocalDateTime;

/** Metadados de um anexo. O conteúdo do arquivo fica no armazenamento, em storedPath. */
public record TaskAttachment(
        Long id,
        String originalName,
        String contentType,
        long sizeBytes,
        String storedPath,
        LocalDateTime createdAt
) {
}
