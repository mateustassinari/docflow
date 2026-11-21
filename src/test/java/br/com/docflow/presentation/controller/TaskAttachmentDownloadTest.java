package br.com.docflow.presentation.controller;

import br.com.docflow.application.port.TaskCalendarDocumentGenerator;
import br.com.docflow.application.service.TaskService;
import br.com.docflow.domain.model.TaskAttachment;
import br.com.docflow.presentation.security.SecurityConfig;
import br.com.docflow.presentation.security.SecurityResponses;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ContentDisposition;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, SecurityResponses.class})
@TestPropertySource(properties = "app.security.password=segredo")
@WithMockUser
class TaskAttachmentDownloadTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskCalendarDocumentGenerator calendarGenerator;

    private void givenAttachment(String originalName, String contentType) throws Exception {
        TaskAttachment attachment = new TaskAttachment(7L, originalName, contentType, 3L, "/uploads/x", null);
        when(taskService.findAttachment(1L, 7L)).thenReturn(attachment);
        when(taskService.loadAttachment(any())).thenReturn("abc".getBytes());
    }

    @Test
    void fileNameShouldNotBeAbleToInjectHeaderParameters() throws Exception {
        String maliciousName = "a\"; filename=evil.html; x=\".txt";
        givenAttachment(maliciousName, "text/plain");

        String header = mockMvc.perform(get("/tasks/1/attachments/7"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getHeader("Content-Disposition");

        // O navegador interpreta o header: o nome inteiro tem de continuar sendo um nome só, sem virar outro parâmetro
        ContentDisposition parsed = ContentDisposition.parse(header);
        assertThat(parsed.isAttachment()).isTrue();
        assertThat(parsed.getFilename()).isEqualTo(maliciousName);
    }

    @Test
    void accentedFileNamesShouldBeEncoded() throws Exception {
        givenAttachment("orçamento.pdf", "application/pdf");

        mockMvc.perform(get("/tasks/1/attachments/7"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("filename*=UTF-8''or%C3%A7amento.pdf")))
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void scriptableTypesShouldBeServedAsGenericDownloads() throws Exception {
        givenAttachment("pagina.html", "text/html");

        mockMvc.perform(get("/tasks/1/attachments/7"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"));
    }

    @Test
    void invalidTypesShouldNotBreakTheDownload() throws Exception {
        givenAttachment("arquivo.bin", "lixo");

        mockMvc.perform(get("/tasks/1/attachments/7"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"));
    }
}
