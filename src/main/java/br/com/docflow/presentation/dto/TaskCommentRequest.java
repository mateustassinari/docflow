package br.com.docflow.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskCommentRequest {
    @NotBlank(message = "O comentário é obrigatório")
    private String texto;
}
