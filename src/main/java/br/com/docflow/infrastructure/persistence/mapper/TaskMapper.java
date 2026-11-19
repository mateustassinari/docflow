package br.com.docflow.infrastructure.persistence.mapper;

import br.com.docflow.domain.model.ChecklistItem;
import br.com.docflow.domain.model.Task;
import br.com.docflow.domain.model.TaskAttachment;
import br.com.docflow.domain.model.TaskAutomation;
import br.com.docflow.domain.model.TaskComment;
import br.com.docflow.infrastructure.persistence.entity.EventEntity;
import br.com.docflow.infrastructure.persistence.entity.TaskAttachmentEntity;
import br.com.docflow.infrastructure.persistence.entity.TaskChecklistItemEntity;
import br.com.docflow.infrastructure.persistence.entity.TaskCommentEntity;
import br.com.docflow.infrastructure.persistence.entity.TaskEntity;
import br.com.docflow.infrastructure.persistence.entity.TaskLabelEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class TaskMapper {

    public Task toDomain(TaskEntity entity) {
        Task task = new Task();
        task.setId(entity.getId());
        task.setEventId(entity.getEvent() != null ? entity.getEvent().getId() : null);
        task.setTitulo(entity.getTitulo());
        task.setDescricao(entity.getDescricao());
        task.setPrazoData(entity.getPrazoData());
        task.setPrazoDataFim(entity.getPrazoDataFim());
        task.setPrazoHorario(entity.getPrazoHorario());
        task.setPrazoHorarioFim(entity.getPrazoHorarioFim());
        task.setAllDay(entity.isAllDay());
        task.setStatus(entity.getStatus());
        task.setTipoServico(entity.getTipoServico());
        task.setAutomation(toAutomation(entity));
        task.setChecklist(new ArrayList<>(entity.getChecklistItems().stream()
                .map(item -> new ChecklistItem(item.getId(), item.getTexto(), item.isConcluido(), item.getOrdem()))
                .toList()));
        task.setEtiquetas(new ArrayList<>(entity.getLabels().stream().map(TaskLabelEntity::getLabel).toList()));
        task.setComments(new ArrayList<>(entity.getComments().stream()
                .map(comment -> new TaskComment(comment.getId(), comment.getTexto(), comment.getCreatedAt(), comment.getUpdatedAt()))
                .toList()));
        task.setAttachments(new ArrayList<>(entity.getAttachments().stream()
                .map(attachment -> new TaskAttachment(
                        attachment.getId(),
                        attachment.getOriginalName(),
                        attachment.getContentType(),
                        attachment.getSizeBytes(),
                        attachment.getStoredPath(),
                        attachment.getCreatedAt()))
                .toList()));
        task.setCreatedAt(entity.getCreatedAt());
        task.setUpdatedAt(entity.getUpdatedAt());
        return task;
    }

    /**
     * Copia o estado do domínio para uma entidade nova ou já carregada.
     * <p>
     * Checklist e etiquetas são recriados (como sempre foram). Comentários e anexos são
     * conciliados por id para preservar as linhas existentes e suas datas: com
     * {@code orphanRemoval}, recriar a coleção apagaria e reinseriria tudo.
     */
    public void mergeInto(Task task, TaskEntity entity, EventEntity event) {
        entity.setEvent(event);
        entity.setTitulo(task.getTitulo());
        entity.setDescricao(task.getDescricao());
        entity.setPrazoData(task.getPrazoData());
        entity.setPrazoDataFim(task.getPrazoDataFim());
        entity.setPrazoHorario(task.getPrazoHorario());
        entity.setPrazoHorarioFim(task.getPrazoHorarioFim());
        entity.setAllDay(task.isAllDay());
        entity.setStatus(task.getStatus());
        entity.setTipoServico(task.getTipoServico());

        TaskAutomation automation = task.getAutomation();
        entity.setAutomationSource(automation != null ? automation.source() : null);
        entity.setAutomationContractId(automation != null ? automation.contractId() : null);
        entity.setAutomationKey(automation != null ? automation.key() : null);

        replaceChecklist(entity, task.getChecklist());
        replaceLabels(entity, task.getEtiquetas());
        mergeComments(entity, task.getComments());
        mergeAttachments(entity, task.getAttachments());
    }

    private TaskAutomation toAutomation(TaskEntity entity) {
        if (entity.getAutomationSource() == null && entity.getAutomationContractId() == null && entity.getAutomationKey() == null) {
            return null;
        }
        return new TaskAutomation(entity.getAutomationSource(), entity.getAutomationContractId(), entity.getAutomationKey());
    }

    private void replaceChecklist(TaskEntity entity, List<ChecklistItem> items) {
        entity.getChecklistItems().clear();
        for (ChecklistItem item : items) {
            TaskChecklistItemEntity itemEntity = new TaskChecklistItemEntity();
            itemEntity.setTask(entity);
            itemEntity.setTexto(item.texto());
            itemEntity.setConcluido(item.concluido());
            itemEntity.setOrdem(item.ordem());
            entity.getChecklistItems().add(itemEntity);
        }
    }

    private void replaceLabels(TaskEntity entity, List<String> labels) {
        entity.getLabels().clear();
        for (String label : labels) {
            TaskLabelEntity labelEntity = new TaskLabelEntity();
            labelEntity.setTask(entity);
            labelEntity.setLabel(label);
            entity.getLabels().add(labelEntity);
        }
    }

    private void mergeComments(TaskEntity entity, List<TaskComment> comments) {
        Map<Long, TaskCommentEntity> existing = new HashMap<>();
        entity.getComments().forEach(comment -> existing.put(comment.getId(), comment));

        List<TaskCommentEntity> merged = new ArrayList<>();
        for (TaskComment comment : comments) {
            TaskCommentEntity commentEntity = comment.id() != null ? existing.get(comment.id()) : null;
            if (commentEntity == null) {
                commentEntity = new TaskCommentEntity();
                commentEntity.setTask(entity);
            }
            if (!Objects.equals(commentEntity.getTexto(), comment.texto())) {
                commentEntity.setTexto(comment.texto());
            }
            merged.add(commentEntity);
        }
        // Comparação por identidade: entidades @Data têm equals por campo.
        entity.getComments().removeIf(current -> merged.stream().noneMatch(kept -> kept == current));
        merged.stream().filter(comment -> comment.getId() == null).forEach(entity.getComments()::add);
    }

    private void mergeAttachments(TaskEntity entity, List<TaskAttachment> attachments) {
        Map<Long, TaskAttachmentEntity> existing = new HashMap<>();
        entity.getAttachments().forEach(attachment -> existing.put(attachment.getId(), attachment));

        List<TaskAttachmentEntity> merged = new ArrayList<>();
        for (TaskAttachment attachment : attachments) {
            TaskAttachmentEntity attachmentEntity = attachment.id() != null ? existing.get(attachment.id()) : null;
            if (attachmentEntity == null) {
                attachmentEntity = new TaskAttachmentEntity();
                attachmentEntity.setTask(entity);
                attachmentEntity.setOriginalName(attachment.originalName());
                attachmentEntity.setContentType(attachment.contentType());
                attachmentEntity.setSizeBytes(attachment.sizeBytes());
                attachmentEntity.setStoredPath(attachment.storedPath());
            }
            merged.add(attachmentEntity);
        }
        entity.getAttachments().removeIf(current -> merged.stream().noneMatch(kept -> kept == current));
        merged.stream().filter(attachment -> attachment.getId() == null).forEach(entity.getAttachments()::add);
    }
}
