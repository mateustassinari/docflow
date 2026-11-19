package br.com.docflow.infrastructure.external.storage;

import br.com.docflow.application.port.TaskAttachmentStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class TaskAttachmentStorageService implements TaskAttachmentStorage {

    private final String uploadDir;

    public TaskAttachmentStorageService(@Value("${app.storage.tasks-dir:uploads/tasks}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public String save(byte[] bytes, String originalName) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String cleanName = StringUtils.cleanPath(originalName == null ? "anexo" : originalName);
        String extension = "";
        int dot = cleanName.lastIndexOf('.');
        if (dot >= 0 && dot < cleanName.length() - 1) {
            extension = cleanName.substring(dot);
        }

        Path target = uploadPath.resolve(UUID.randomUUID() + extension);
        Files.write(target, bytes);
        return target.toAbsolutePath().toString();
    }

    @Override
    public byte[] load(String storedPath) throws IOException {
        return Files.readAllBytes(Paths.get(storedPath));
    }

    @Override
    public void deleteIfExists(String storedPath) throws IOException {
        if (storedPath != null && !storedPath.isBlank()) {
            Files.deleteIfExists(Paths.get(storedPath));
        }
    }
}
