package br.com.docflow.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiChatRequest {

    @NotBlank(message = "A pergunta é obrigatória")
    @Size(max = 2000, message = "A pergunta pode ter até 2000 caracteres")
    private String question;
}
