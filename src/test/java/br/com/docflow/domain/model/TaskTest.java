package br.com.docflow.domain.model;

import br.com.docflow.domain.exception.NotFoundException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskTest {

    @Test
    void effectiveDeadlineShouldPreferEndDate() {
        Task task = new Task();
        task.setPrazoData(LocalDate.of(2026, 11, 10));
        assertThat(task.effectiveDeadline()).isEqualTo(LocalDate.of(2026, 11, 10));

        task.setPrazoDataFim(LocalDate.of(2026, 11, 15));
        assertThat(task.effectiveDeadline())
                .as("tarefa com período vence no último dia, não no primeiro")
                .isEqualTo(LocalDate.of(2026, 11, 15));
    }

    @Test
    void shouldDetectPaintingMaterialsAutomation() {
        Task task = new Task();
        assertThat(task.isPaintingMaterialsAutomation()).isFalse();

        task.setAutomation(new TaskAutomation("CONTRACT_SIGNED", 1L, "PINTURA_COMPRA_MATERIAL"));
        assertThat(task.isPaintingMaterialsAutomation()).isTrue();

        task.setAutomation(new TaskAutomation("CONTRACT_SIGNED", 1L, "AQUARELA_COMPRA_MATERIAL"));
        assertThat(task.isPaintingMaterialsAutomation()).isFalse();
    }

    @Test
    void shouldDetectCompletedMolduraCaixaOnlyWhenChecked() {
        Task task = new Task();
        task.replaceChecklist(List.of(new ChecklistItem(null, "Moldura e Caixa?", false, null)));
        assertThat(task.hasCompletedMolduraCaixa()).isFalse();

        task.replaceChecklist(List.of(new ChecklistItem(null, "Moldura e Caixa?", true, null)));
        assertThat(task.hasCompletedMolduraCaixa()).isTrue();
    }

    @Test
    void replaceChecklistShouldSkipBlankItemsTrimTextAndKeepOriginalPositionAsOrder() {
        Task task = new Task();
        task.replaceChecklist(List.of(
                new ChecklistItem(null, "  Tela ", false, null),
                new ChecklistItem(null, "   ", false, null),
                new ChecklistItem(null, "Tintas", true, null),
                new ChecklistItem(null, "Moldura", false, 9)));

        assertThat(task.getChecklist()).containsExactly(
                new ChecklistItem(null, "Tela", false, 0),
                new ChecklistItem(null, "Tintas", true, 2),
                new ChecklistItem(null, "Moldura", false, 9));
    }

    @Test
    void replaceLabelsShouldTrimAndDropBlankAndDuplicated() {
        Task task = new Task();
        task.replaceLabels(Arrays.asList(" urgente ", "", null, "urgente", "cliente"));

        assertThat(task.getEtiquetas()).containsExactly("urgente", "cliente");
    }

    @Test
    void shouldAddUpdateAndRemoveComments() {
        Task task = new Task();
        task.setComments(new java.util.ArrayList<>(List.of(new TaskComment(5L, "antigo", null, null))));

        task.addComment("  novo  ");
        task.updateComment(5L, " editado ");

        assertThat(task.getComments()).extracting(TaskComment::texto).containsExactly("editado", "novo");
        assertThat(task.getComments().get(0).id()).isEqualTo(5L);

        task.removeComment(5L);
        assertThat(task.getComments()).extracting(TaskComment::texto).containsExactly("novo");
    }

    @Test
    void shouldFailWithDomainErrorWhenCommentOrAttachmentIsMissing() {
        Task task = new Task();

        assertThatThrownBy(() -> task.updateComment(1L, "x"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Comentário não encontrado");
        assertThatThrownBy(() -> task.findAttachment(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Anexo não encontrado");
    }
}
