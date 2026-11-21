package br.com.docflow.presentation.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Login com sessão no servidor e cookie HttpOnly (motivo em docs/adr/0001-autenticacao-com-sessao.md).
 *
 * <p>O frontend fica na mesma origem da API (proxy do nginx no Docker, do Vite em desenvolvimento),
 * então o navegador manda o cookie de sessão sozinho, inclusive nos PDFs abertos com window.open.
 * Toda resposta de segurança sai em JSON, no mesmo formato de erro do resto da API.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityResponses responses) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        // Diz ao frontend se há sessão e entrega o cookie do token CSRF antes do login
                        .requestMatchers("/auth/session").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        // Uma loginPage própria desliga a página de login padrão do Spring: quem desenha a tela é o React
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(responses::loginSucceeded)
                        .failureHandler(responses::loginFailed)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(responses::loggedOut))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(responses::unauthorized)
                        .accessDeniedHandler(responses::forbidden))
                // O frontend mostra os PDFs da API dentro de iframes: liberado só para a própria origem
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                // Token CSRF num cookie legível pelo JavaScript, devolvido no header X-XSRF-TOKEN
                .csrf(csrf -> csrf.spa())
                // A API responde 401 em vez de redirecionar, então não há requisição para guardar e repetir após o login
                .requestCache(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    /**
     * Usuário único, vindo da configuração (APP_USERNAME e APP_PASSWORD). A senha pode vir pronta com hash
     * ({@code {bcrypt}$2a$...}); em texto puro, recebe o hash BCrypt ao subir e só o hash fica em memória.
     */
    @Bean
    UserDetailsService userDetailsService(
            @Value("${app.security.username:admin}") String username,
            @Value("${app.security.password:}") String password,
            PasswordEncoder passwordEncoder
    ) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Defina a senha de acesso em APP_PASSWORD (veja o .env.example).");
        }
        String storedPassword = password.startsWith("{") ? password : passwordEncoder.encode(password);
        return new InMemoryUserDetailsManager(User.withUsername(username).password(storedPassword).roles("USER").build());
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
