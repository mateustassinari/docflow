package br.com.docflow.application.usecase.command;

/** Arquivo enviado para anexar a uma tarefa, já lido da requisição HTTP. */
public record AttachmentUpload(String originalName, String contentType, byte[] bytes) {
}
