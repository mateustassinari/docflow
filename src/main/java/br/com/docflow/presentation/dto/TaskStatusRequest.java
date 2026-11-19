package br.com.docflow.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskStatusRequest {
    @NotBlank(message = "O status é obrigatório")
    private String status;
}
