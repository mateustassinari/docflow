package br.com.docflow.infrastructure.external.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class TaskAttachmentStorageServiceTest {

    @TempDir
    Path uploadDir;

    @Test
    void shouldKeepOnlySimpleExtensions() {
        assertThat(TaskAttachmentStorageService.safeExtension("foto.JPG")).isEqualTo(".jpg");
        assertThat(TaskAttachmentStorageService.safeExtension("orcamento.final.pdf")).isEqualTo(".pdf");
        assertThat(TaskAttachmentStorageService.safeExtension("sem-extensao")).isEmpty();
        assertThat(TaskAttachmentStorageService.safeExtension("a./../../etc/passwd")).isEmpty();
        assertThat(TaskAttachmentStorageService.safeExtension("x.tar.gz;rm -rf")).isEmpty();
        assertThat(TaskAttachmentStorageService.safeExtension(null)).isEmpty();
    }

    @Test
    void shouldSaveInsideTheUploadFolderWhateverTheOriginalName() throws Exception {
        TaskAttachmentStorageService storage = new TaskAttachmentStorageService(uploadDir.toString());

        String stored = storage.save("conteudo".getBytes(), "../../../../tmp/evil.sh/../x.txt");

        Path path = Path.of(stored);
        assertThat(path.getParent()).isEqualTo(uploadDir.toAbsolutePath());
        assertThat(path.getFileName().toString()).matches("[0-9a-f-]{36}\\.txt");
        assertThat(Files.readString(path)).isEqualTo("conteudo");
    }
}
