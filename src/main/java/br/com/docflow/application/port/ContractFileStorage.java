package br.com.docflow.application.port;

import java.io.IOException;

public interface ContractFileStorage {
    String savePdf(byte[] bytes, String prefix) throws IOException;

    byte[] loadPdf(String path) throws IOException;

    String overwritePdf(String path, byte[] bytes) throws IOException;

    void deletePdfIfExists(String path) throws IOException;
}
