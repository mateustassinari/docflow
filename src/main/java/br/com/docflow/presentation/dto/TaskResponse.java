package br.com.docflow.presentation.dto;

import br.com.docflow.application.readmodel.TaskEventSummary;
import br.com.docflow.application.readmodel.TaskView;
import br.com.docflow.domain.model.ChecklistItem;
import br.com.docflow.domain.model.TaskAttachment;
import br.com.docflow.domain.model.TaskComment;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class TaskResponse {
    private Long id;
    private EventSummary event;
    private String titulo;
    private String descricao;
    private LocalDate prazoData;
    private LocalDate prazoDataFim;
    private LocalTime prazoHorario;
    private LocalTime prazoHorarioFim;
    private String status;
    private String tipoServico;
    private List<String> etiquetas;
    private List<ChecklistItemResponse> checklistItems;
    private List<Comment> comments;
    private List<Attachment> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse from(TaskView view) {
        var task = view.task();
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setEvent(EventSummary.from(view.event()));
        response.setTitulo(task.getTitulo());
        response.setDescricao(task.getDescricao());
        response.setPrazoData(task.getPrazoData());
        response.setPrazoDataFim(task.getPrazoDataFim());
        response.setPrazoHorario(task.getPrazoHorario());
        response.setPrazoHorarioFim(task.getPrazoHorarioFim());
        response.setStatus(task.getStatus() != null ? task.getStatus().name() : null);
        response.setTipoServico(task.getTipoServico());
        response.setEtiquetas(List.copyOf(task.getEtiquetas()));
        response.setChecklistItems(task.getChecklist().stream().map(ChecklistItemResponse::from).toList());
        response.setComments(task.getComments().stream().map(Comment::from).toList());
        response.setAttachments(task.getAttachments().stream().map(Attachment::from).toList());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
    }

    @Data
    public static class EventSummary {
        private Long id;
        private String clienteNome;
        private String tipoEvento;
        private String tipoServico;
        private LocalDate data;

        static EventSummary from(TaskEventSummary event) {
            if (event == null) return null;
            EventSummary summary = new EventSummary();
            summary.setId(event.id());
            summary.setClienteNome(event.clienteNome());
            summary.setTipoEvento(event.tipoEvento());
            summary.setTipoServico(event.tipoServico());
            summary.setData(event.data());
            return summary;
        }
    }

    /** Item de checklist no JSON (o nome da classe não aparece no JSON). */
    @Data
    public static class ChecklistItemResponse {
        private Long id;
        private String texto;
        private boolean concluido;
        private Integer ordem;

        static ChecklistItemResponse from(ChecklistItem item) {
            ChecklistItemResponse response = new ChecklistItemResponse();
            response.setId(item.id());
            response.setTexto(item.texto());
            response.setConcluido(item.concluido());
            response.setOrdem(item.ordem());
            return response;
        }
    }

    @Data
    public static class Comment {
        private Long id;
        private String texto;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        static Comment from(TaskComment comment) {
            Comment response = new Comment();
            response.setId(comment.id());
            response.setTexto(comment.texto());
            response.setCreatedAt(comment.createdAt());
            response.setUpdatedAt(comment.updatedAt());
            return response;
        }
    }

    @Data
    public static class Attachment {
        private Long id;
        private String originalName;
        private String contentType;
        private long sizeBytes;
        private LocalDateTime createdAt;

        static Attachment from(TaskAttachment attachment) {
            Attachment response = new Attachment();
            response.setId(attachment.id());
            response.setOriginalName(attachment.originalName());
            response.setContentType(attachment.contentType());
            response.setSizeBytes(attachment.sizeBytes());
            response.setCreatedAt(attachment.createdAt());
            return response;
        }
    }
}
