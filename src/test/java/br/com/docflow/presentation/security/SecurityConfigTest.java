package br.com.docflow.presentation.security;

import br.com.docflow.application.service.AiAssistantFacade;
import br.com.docflow.presentation.controller.AiController;
import br.com.docflow.presentation.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, AiController.class})
@Import({SecurityConfig.class, SecurityResponses.class})
@TestPropertySource(properties = {"app.security.username=demo", "app.security.password=segredo"})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiAssistantFacade aiAssistantFacade;

    @Test
    void apiWithoutLoginShouldAnswer401InTheApiErrorFormat() throws Exception {
        mockMvc.perform(get("/ai/status"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("""
                        {"status": 401, "code": "UNAUTHORIZED", "message": "Faça login para continuar."}""", true));
    }

    @Test
    void sessionWithoutLoginShouldSayAnonymousAndHandOutTheCsrfCookie() throws Exception {
        mockMvc.perform(get("/auth/session"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"authenticated": false, "username": null}""", true))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
    }

    @Test
    void loginWithoutCsrfTokenShouldBeRefused() throws Exception {
        mockMvc.perform(post("/auth/login").param("username", "demo").param("password", "segredo"))
                .andExpect(status().isForbidden())
                .andExpect(content().json("""
                        {"status": 403, "code": "CSRF_INVALID"}"""));
    }

    @Test
    void loginWithWrongPasswordShouldAnswer401() throws Exception {
        mockMvc.perform(post("/auth/login").param("username", "demo").param("password", "errada").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("""
                        {"status": 401, "code": "INVALID_CREDENTIALS", "message": "Usuário ou senha inválidos."}""", true));
    }

    @Test
    void loginShouldOpenASessionThatAuthorizesTheApiUntilLogout() throws Exception {
        when(aiAssistantFacade.isEnabled()).thenReturn(true);

        MvcResult login = mockMvc.perform(post("/auth/login").param("username", "demo").param("password", "segredo").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"authenticated": true, "username": "demo"}""", true))
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/ai/status").session(session))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"enabled\": true}", true));
        mockMvc.perform(get("/auth/session").session(session))
                .andExpect(content().json("""
                        {"authenticated": true, "username": "demo"}""", true));

        mockMvc.perform(post("/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent());
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void applicationShouldNotStartWithoutAPassword() {
        SecurityConfig config = new SecurityConfig();

        assertThatThrownBy(() -> config.userDetailsService("demo", "", PasswordEncoderFactories.createDelegatingPasswordEncoder()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_PASSWORD");
    }

    @Test
    void passwordShouldBeKeptOnlyAsAHash() {
        SecurityConfig config = new SecurityConfig();

        String stored = config.userDetailsService("demo", "segredo", PasswordEncoderFactories.createDelegatingPasswordEncoder())
                .loadUserByUsername("demo").getPassword();

        assertThat(stored).startsWith("{bcrypt}").doesNotContain("segredo");
    }
}
