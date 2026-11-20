package br.com.docflow.presentation.dto;

import br.com.docflow.application.readmodel.TaskEventSummary;
import br.com.docflow.application.readmodel.TaskSummary;
import br.com.docflow.application.readmodel.TaskView;
import br.com.docflow.domain.model.ChecklistItem;
import br.com.docflow.domain.model.Task;
import br.com.docflow.domain.model.TaskAttachment;
import br.com.docflow.domain.model.TaskComment;
import br.com.docflow.domain.model.TaskStatus;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Fixa o contrato JSON da API de tarefas (o que o frontend lê), com o ObjectMapper configurado
 * pelo Spring Boot. A refatoração das camadas não pode mudar nenhum campo.
 */
@JsonTest
class TaskResponseJsonTest {

    static final String FULL_TASK_JSON = """
            {
              "id": 1,
              "event": {"id": 10, "clienteNome": "Isabella", "tipoEvento": "Casamento",
                        "tipoServico": "PINTURA_AO_VIVO", "data": "2026-12-20"},
              "titulo": "Compra de material",
              "descricao": "Comprar antes da reunião",
              "prazoData": "2026-12-05",
              "prazoDataFim": "2026-12-06",
              "prazoHorario": "09:00:00",
              "prazoHorarioFim": "18:00:00",
              "status": "EM_ANDAMENTO",
              "tipoServico": "PINTURA_AO_VIVO",
              "etiquetas": ["urgente", "cliente"],
              "checklistItems": [
                {"id": 5, "texto": "Tela", "concluido": true, "ordem": 0},
                {"id": 6, "texto": "Moldura e Caixa?", "concluido": false, "ordem": 1}
              ],
              "comments": [
                {"id": 7, "texto": "Pedir orçamento", "createdAt": "2026-11-01T10:00:00", "updatedAt": "2026-11-02T11:30:00"}
              ],
              "attachments": [
                {"id": 8, "originalName": "orcamento.pdf", "contentType": "application/pdf", "sizeBytes": 123,
                 "createdAt": "2026-11-03T12:00:00"}
              ],
              "createdAt": "2026-11-01T09:00:00",
              "updatedAt": "2026-11-04T09:00:00"
            }
            """;

    static final String TASK_WITHOUT_EVENT_JSON = """
            {
              "id": 2, "event": null, "titulo": "Avulsa", "descricao": null,
              "prazoData": "2026-12-05", "prazoDataFim": null, "prazoHorario": "14:00:00", "prazoHorarioFim": "15:00:00",
              "status": "A_FAZER", "tipoServico": null, "etiquetas": [], "checklistItems": [], "comments": [],
              "attachments": [], "createdAt": null, "updatedAt": null
            }
            """;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullTaskShouldKeepTheJsonContract() throws Exception {
        JSONAssert.assertEquals(FULL_TASK_JSON, objectMapper.writeValueAsString(TaskResponse.from(fullTask())), JSONCompareMode.STRICT);
    }

    @Test
    void taskWithoutEventShouldKeepTheJsonContract() throws Exception {
        JSONAssert.assertEquals(TASK_WITHOUT_EVENT_JSON, objectMapper.writeValueAsString(TaskResponse.from(taskWithoutEvent())), JSONCompareMode.STRICT);
    }

    @Test
    void summaryShouldKeepTheJsonContract() throws Exception {
        JSONAssert.assertEquals("{\"overdue\": 1, \"today\": 2, \"next7Days\": 3}",
                objectMapper.writeValueAsString(TaskSummaryResponse.from(new TaskSummary(1, 2, 3))), JSONCompareMode.STRICT);
    }

    private TaskView fullTask() {
        Task task = new Task();
        task.setId(1L);
        task.setEventId(10L);
        task.setTitulo("Compra de material");
        task.setDescricao("Comprar antes da reunião");
        task.setPrazoData(LocalDate.of(2026, 12, 5));
        task.setPrazoDataFim(LocalDate.of(2026, 12, 6));
        task.setPrazoHorario(LocalTime.of(9, 0));
        task.setPrazoHorarioFim(LocalTime.of(18, 0));
        task.setStatus(TaskStatus.EM_ANDAMENTO);
        task.setTipoServico("PINTURA_AO_VIVO");
        task.setCreatedAt(LocalDateTime.of(2026, 11, 1, 9, 0));
        task.setUpdatedAt(LocalDateTime.of(2026, 11, 4, 9, 0));
        task.setEtiquetas(new ArrayList<>(List.of("urgente", "cliente")));
        task.setChecklist(new ArrayList<>(List.of(
                new ChecklistItem(5L, "Tela", true, 0),
                new ChecklistItem(6L, "Moldura e Caixa?", false, 1))));
        task.setComments(new ArrayList<>(List.of(new TaskComment(7L, "Pedir orçamento",
                LocalDateTime.of(2026, 11, 1, 10, 0), LocalDateTime.of(2026, 11, 2, 11, 30)))));
        task.setAttachments(new ArrayList<>(List.of(new TaskAttachment(8L, "orcamento.pdf", "application/pdf", 123,
                "/uploads/tasks/x.pdf", LocalDateTime.of(2026, 11, 3, 12, 0)))));
        TaskEventSummary event = new TaskEventSummary(10L, "Isabella", "Casamento", "PINTURA_AO_VIVO", LocalDate.of(2026, 12, 20));
        return new TaskView(task, event);
    }

    private TaskView taskWithoutEvent() {
        Task task = new Task();
        task.setId(2L);
        task.setTitulo("Avulsa");
        task.setPrazoData(LocalDate.of(2026, 12, 5));
        task.setPrazoHorario(LocalTime.of(14, 0));
        task.setPrazoHorarioFim(LocalTime.of(15, 0));
        return new TaskView(task, null);
    }
}
