package br.com.docflow.presentation.controller;

import br.com.docflow.presentation.dto.SessionResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login e logout são tratados pelos filtros do Spring Security (POST /auth/login e /auth/logout,
 * em SecurityConfig). Aqui fica só a consulta da sessão, que o frontend faz ao abrir e após o login.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/session")
    public SessionResponse session(@AuthenticationPrincipal UserDetails user, CsrfToken csrfToken) {
        // Ler o token faz o Spring gravar o cookie XSRF-TOKEN, que o frontend devolve no header das escritas.
        // Ele muda a cada login e logout, por isso o frontend consulta a sessão de novo depois de cada um.
        csrfToken.getToken();
        return user == null ? SessionResponse.anonymous() : SessionResponse.authenticated(user.getUsername());
    }
}
