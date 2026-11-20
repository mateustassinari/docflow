package br.com.docflow.presentation.security;

import br.com.docflow.presentation.dto.SessionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Respostas JSON do login, do logout e das recusas de acesso, no formato de erro da API. */
@Component
public class SecurityResponses {

    private final ObjectMapper objectMapper;

    public SecurityResponses(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void loginSucceeded(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        write(response, HttpStatus.OK, SessionResponse.authenticated(authentication.getName()));
    }

    void loginFailed(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED, error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Usuário ou senha inválidos."));
    }

    void loggedOut(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    void unauthorized(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED, error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Faça login para continuar."));
    }

    void forbidden(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
        if (exception instanceof CsrfException) {
            write(response, HttpStatus.FORBIDDEN, error(HttpStatus.FORBIDDEN, "CSRF_INVALID",
                    "Sessão expirada ou requisição inválida. Recarregue a página."));
            return;
        }
        write(response, HttpStatus.FORBIDDEN, error(HttpStatus.FORBIDDEN, "FORBIDDEN", "Acesso negado."));
    }

    private Map<String, Object> error(HttpStatus status, String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("code", code);
        body.put("message", message);
        return body;
    }

    private void write(HttpServletResponse response, HttpStatus status, Object body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
