package br.com.docflow.domain.model;

import br.com.docflow.domain.exception.NotFoundException;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tarefa da agenda, com checklist, etiquetas, comentários e anexos. O evento é referenciado
 * por id: a tarefa não altera o evento.
 */
@Data
public class Task {

    public static final String PAINTING_MATERIALS_KEY = "PINTURA_COMPRA_MATERIAL";

    private Long id;
    private Long eventId;
    private String titulo;
    private String descricao;
    private LocalDate prazoData;
    private LocalDate prazoDataFim;
    private LocalTime prazoHorario;
    private LocalTime prazoHorarioFim;
    private boolean allDay;
    private TaskStatus status = TaskStatus.A_FAZER;
    private String tipoServico;
    private TaskAutomation automation;
    private List<ChecklistItem> checklist = new ArrayList<>();
    private List<String> etiquetas = new ArrayList<>();
    private List<TaskComment> comments = new ArrayList<>();
    private List<TaskAttachment> attachments = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Prazo que vale para atraso e lembretes: o fim, se houver; senão o início. */
    public LocalDate effectiveDeadline() {
        return prazoDataFim != null ? prazoDataFim : prazoData;
    }

    public boolean isPaintingMaterialsAutomation() {
        return automation != null && PAINTING_MATERIALS_KEY.equals(automation.key());
    }

    public boolean hasCompletedMolduraCaixa() {
        return checklist.stream().anyMatch(item -> item.concluido() && item.isMolduraCaixa());
    }

    /**
     * Substitui o checklist: ignora itens sem texto, remove espaços nas pontas e, sem ordem
     * informada, usa a posição do item na lista recebida.
     */
    public void replaceChecklist(List<ChecklistItem> items) {
        List<ChecklistItem> safeItems = items == null ? List.of() : items;
        List<ChecklistItem> normalized = new ArrayList<>();
        for (int index = 0; index < safeItems.size(); index++) {
            ChecklistItem item = safeItems.get(index);
            if (item.texto() == null || item.texto().isBlank()) {
                continue;
            }
            normalized.add(new ChecklistItem(
                    item.id(),
                    item.texto().trim(),
                    item.concluido(),
                    item.ordem() == null ? index : item.ordem()));
        }
        checklist = normalized;
    }

    /** Substitui as etiquetas: sem espaços nas pontas, sem vazias e sem repetidas. */
    public void replaceLabels(List<String> labels) {
        etiquetas = labels == null ? new ArrayList<>() : new ArrayList<>(labels.stream()
                .map(label -> label == null ? "" : label.trim())
                .filter(label -> !label.isBlank())
                .distinct()
                .toList());
    }

    public void addComment(String texto) {
        comments.add(new TaskComment(null, texto.trim(), null, null));
    }

    public void updateComment(Long commentId, String texto) {
        TaskComment current = findComment(commentId);
        comments.set(comments.indexOf(current),
                new TaskComment(current.id(), texto.trim(), current.createdAt(), current.updatedAt()));
    }

    public void removeComment(Long commentId) {
        comments.removeIf(comment -> commentId.equals(comment.id()));
    }

    public void addAttachment(TaskAttachment attachment) {
        attachments.add(attachment);
    }

    public TaskAttachment findAttachment(Long attachmentId) {
        return attachments.stream()
                .filter(attachment -> attachmentId.equals(attachment.id()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("TASK_ATTACHMENT_NOT_FOUND", "Anexo não encontrado"));
    }

    public void removeAttachment(Long attachmentId) {
        attachments.removeIf(attachment -> attachmentId.equals(attachment.id()));
    }

    private TaskComment findComment(Long commentId) {
        return comments.stream()
                .filter(comment -> commentId.equals(comment.id()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("TASK_COMMENT_NOT_FOUND", "Comentário não encontrado"));
    }
}
