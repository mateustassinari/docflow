package br.com.docflow.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskCommentRequest {
    @NotBlank(message = "O comentário é obrigatório")
    @Size(max = 5000, message = "O comentário pode ter até 5000 caracteres")
    private String texto;
}
