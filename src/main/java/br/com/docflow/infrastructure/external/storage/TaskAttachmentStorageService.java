package br.com.docflow.infrastructure.external.storage;

import br.com.docflow.application.port.TaskAttachmentStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class TaskAttachmentStorageService implements TaskAttachmentStorage {

    private static final Pattern SAFE_EXTENSION = Pattern.compile("[A-Za-z0-9]{1,10}");

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

        // O nome em disco é um UUID gerado aqui; do nome original só fica a extensão, e só se for simples
        Path target = uploadPath.resolve(UUID.randomUUID() + safeExtension(originalName));
        Files.write(target, bytes);
        return target.toAbsolutePath().toString();
    }

    static String safeExtension(String originalName) {
        if (originalName == null) {
            return "";
        }
        int dot = originalName.lastIndexOf('.');
        String extension = dot >= 0 ? originalName.substring(dot + 1) : "";
        return SAFE_EXTENSION.matcher(extension).matches() ? "." + extension.toLowerCase() : "";
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
