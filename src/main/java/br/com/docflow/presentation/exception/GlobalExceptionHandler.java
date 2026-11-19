package br.com.docflow.presentation.exception;

import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final String maxUploadSize;

    public GlobalExceptionHandler(@Value("${spring.servlet.multipart.max-file-size:20MB}") String maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    /** Arquivo acima do limite de upload: 413 com o limite na mensagem, em vez de um erro genérico. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(baseResponse(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE",
                        "Arquivo maior que o limite de " + maxUploadSize + "."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = baseResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Falha de validação nos dados fornecidos.");

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(baseResponse(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest()
                .body(baseResponse(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(baseResponse(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION", "A operação viola uma regra de integridade dos dados."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeExceptions(RuntimeException ex) {
        // A resposta não expõe detalhes internos, mas o erro precisa ficar no log para ser investigado
        log.error("Erro inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(baseResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Erro interno do servidor."));
    }

    private Map<String, Object> baseResponse(HttpStatus status, String code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("code", code);
        response.put("message", message);
        return response;
    }
}
