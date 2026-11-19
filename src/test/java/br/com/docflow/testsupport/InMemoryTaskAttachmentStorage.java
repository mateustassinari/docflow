package br.com.docflow.testsupport;

import br.com.docflow.application.port.TaskAttachmentStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryTaskAttachmentStorage implements TaskAttachmentStorage {

    private final Map<String, byte[]> files = new HashMap<>();
    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public String save(byte[] bytes, String originalName) {
        String path = "/mem/" + counter.incrementAndGet() + "-" + originalName;
        files.put(path, bytes);
        return path;
    }

    @Override
    public byte[] load(String storedPath) {
        return files.get(storedPath);
    }

    @Override
    public void deleteIfExists(String storedPath) {
        files.remove(storedPath);
    }

    public boolean exists(String storedPath) {
        return files.containsKey(storedPath);
    }
}
