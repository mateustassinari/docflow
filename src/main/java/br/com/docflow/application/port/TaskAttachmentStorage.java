package br.com.docflow.application.port;

import java.io.IOException;

/** Armazenamento dos arquivos anexados às tarefas. */
public interface TaskAttachmentStorage {

    /** Grava o arquivo e devolve o caminho a guardar no anexo. */
    String save(byte[] bytes, String originalName) throws IOException;

    byte[] load(String storedPath) throws IOException;

    void deleteIfExists(String storedPath) throws IOException;
}
