package br.com.docflow.infrastructure.external.document;

import br.com.docflow.application.readmodel.TaskEventSummary;
import br.com.docflow.application.readmodel.TaskView;
import br.com.docflow.domain.model.Task;
import br.com.docflow.domain.model.TaskStatus;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenPdfTaskCalendarGeneratorTest {

    private final OpenPdfTaskCalendarGenerator generator = new OpenPdfTaskCalendarGenerator();

    @Test
    void monthShouldShowTaskTitleServiceAndPeriodOnEachDay() throws Exception {
        String text = text(generator.generate(List.of(
                task("Reunião com os noivos", LocalDate.of(2026, 11, 3), null, "PINTURA_AO_VIVO", "Isabella"),
                task("Material", LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 5), "AQUARELA_AO_VIVO", null)),
                "month", LocalDate.of(2026, 11, 17)));

        assertThat(text).contains("Calendário de Tarefas", "novembro de 2026", "Reunião com os noivos", "Pintura ao Vivo");
        assertThat(text.split("Material", -1))
                .as("tarefa com período aparece em cada dia, de 03 a 05/11")
                .hasSize(4);
    }

    @Test
    void dayShouldShowClientAndEventDate() throws Exception {
        String text = text(generator.generate(List.of(
                task("Briefing", LocalDate.of(2026, 11, 18), null, "IDENTIDADE_VISUAL", "Anna")),
                "timeGridDay", LocalDate.of(2026, 11, 18)));

        assertThat(text).contains("Briefing", "Identidade visual", "Anna - 01/12/2026");
    }

    @Test
    void emptyPeriodShouldStillGenerateAPdf() throws Exception {
        assertThat(text(generator.generate(List.of(), "timeGridWeek", LocalDate.of(2026, 11, 15))))
                .contains("Calendário de Tarefas");
    }

    private TaskView task(String title, LocalDate start, LocalDate end, String service, String clientName) {
        Task task = new Task();
        task.setId((long) title.hashCode());
        task.setTitulo(title);
        task.setPrazoData(start);
        task.setPrazoDataFim(end);
        task.setPrazoHorario(LocalTime.of(9, 0));
        task.setStatus(TaskStatus.A_FAZER);
        task.setTipoServico(service);
        TaskEventSummary event = clientName == null ? null
                : new TaskEventSummary(10L, clientName, "Casamento", service, LocalDate.of(2026, 12, 1));
        return new TaskView(task, event);
    }

    private String text(byte[] pdf) throws Exception {
        PdfReader reader = new PdfReader(pdf);
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        StringBuilder text = new StringBuilder();
        for (int page = 1; page <= reader.getNumberOfPages(); page++) {
            text.append(extractor.getTextFromPage(page)).append('\n');
        }
        return text.toString();
    }
}
