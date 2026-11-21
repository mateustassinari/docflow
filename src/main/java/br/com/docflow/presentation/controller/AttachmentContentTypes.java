package br.com.docflow.presentation.controller;

import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

import java.util.Set;

/**
 * Tipos de anexo que a API devolve como vieram. O tipo é informado pelo navegador de quem enviou,
 * então não é confiável: qualquer outro (HTML, SVG, XML, scripts ou um valor inválido) é devolvido
 * como download genérico, para o navegador nunca interpretar o conteúdo.
 */
final class AttachmentContentTypes {

    private static final Set<String> ALLOWED = Set.of(
            "application/pdf",
            "image/png", "image/jpeg", "image/gif", "image/webp",
            "text/plain", "text/csv",
            "application/zip",
            "application/msword",
            "application/vnd.ms-excel",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.spreadsheet"
    );

    private AttachmentContentTypes() {
    }

    static MediaType safe(String declared) {
        if (declared == null || declared.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            MediaType type = MediaType.parseMediaType(declared);
            String essence = type.getType() + "/" + type.getSubtype();
            return ALLOWED.contains(essence.toLowerCase()) ? new MediaType(type.getType(), type.getSubtype()) : MediaType.APPLICATION_OCTET_STREAM;
        } catch (InvalidMediaTypeException ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
