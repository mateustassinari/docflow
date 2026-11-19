package br.com.docflow.infrastructure.external.storage;

import br.com.docflow.application.port.ContractFileStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements ContractFileStorage {

    private final String uploadDir;

    public LocalStorageService(@Value("${app.storage.contracts-dir:uploads/contracts}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public String savePdf(byte[] pdfBytes, String prefix) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = prefix + "_" + UUID.randomUUID().toString() + ".pdf";
        Path filePath = uploadPath.resolve(fileName);
        
        Files.write(filePath, pdfBytes);
        
        // Retorna um caminho que pode ser salvo no banco e exposto via API dps
        return filePath.toAbsolutePath().toString();
    }
    
    @Override
    public byte[] loadPdf(String pdfUrl) throws IOException {
        Path path = Paths.get(pdfUrl);
        return Files.readAllBytes(path);
    }

    @Override
    public String overwritePdf(String pdfUrl, byte[] pdfBytes) throws IOException {
        Path path = Paths.get(pdfUrl);
        Path parent = path.getParent();

        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        Files.write(path, pdfBytes);
        return path.toAbsolutePath().toString();
    }

    @Override
    public void deletePdfIfExists(String pdfUrl) throws IOException {
        if (pdfUrl == null || pdfUrl.isBlank()) {
            return;
        }

        Files.deleteIfExists(Paths.get(pdfUrl));
    }
}
