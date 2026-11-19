package br.com.docflow.presentation.controller;

import br.com.docflow.application.service.AiAssistantFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiAssistantFacade aiAssistantFacade;

    @Test
    void statusShouldTellTheFrontendWhetherToShowTheAssistant() throws Exception {
        when(aiAssistantFacade.isEnabled()).thenReturn(false);

        mockMvc.perform(get("/ai/status"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"enabled\": false}", true));
    }
}
