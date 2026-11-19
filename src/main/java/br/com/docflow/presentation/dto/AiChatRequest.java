package br.com.docflow.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiChatRequest {

    @NotBlank(message = "A pergunta é obrigatória")
    private String question;
}
