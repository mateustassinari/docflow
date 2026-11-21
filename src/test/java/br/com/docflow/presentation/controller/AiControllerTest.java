package br.com.docflow.presentation.controller;

import br.com.docflow.application.service.AiAssistantFacade;
import br.com.docflow.presentation.security.SecurityConfig;
import br.com.docflow.presentation.security.SecurityResponses;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
@Import({SecurityConfig.class, SecurityResponses.class})
@TestPropertySource(properties = "app.security.password=segredo")
@WithMockUser
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiAssistantFacade aiAssistantFacade;

    @Test
    void statusShouldTellTheFrontendWhetherToShowTheAssistant() throws Exception {
        when(aiAssistantFacade.isEnabled()).thenReturn(false);

        mockMvc.perform(get("/ai/status"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"enabled\": false}", true));
    }

    @Test
    void chatShouldRejectHugeQuestionsBeforeReachingTheModel() throws Exception {
        String question = "a".repeat(2001);

        mockMvc.perform(post("/ai/chat").with(csrf())
                        .contentType("application/json")
                        .content("{\"question\": \"" + question + "\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(aiAssistantFacade);
    }
}
