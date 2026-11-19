package br.com.docflow.presentation.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler("20MB");

    @Test
    void fileAboveTheLimitShouldReturn413WithTheLimitInTheMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleMaxUploadSize(new MaxUploadSizeExceededException(20L * 1024 * 1024));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody())
                .containsEntry("code", "FILE_TOO_LARGE")
                .containsEntry("message", "Arquivo maior que o limite de 20MB.");
    }

    @Test
    void unexpectedErrorShouldNotExposeDetails() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleRuntimeExceptions(new IllegalStateException("detalhe interno"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("message", "Erro interno do servidor.");
    }
}
