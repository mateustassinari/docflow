package br.com.docflow.presentation.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskChecklistItemRequest {
    private Long id;
    @Size(max = 255, message = "O item do checklist pode ter até 255 caracteres")
    private String texto;
    private boolean concluido;
    private Integer ordem;
}
