package br.com.docflow.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateContractRequest {

    @NotNull(message = "O ID do evento é obrigatório")
    private Long eventId;

    @NotNull(message = "O ID do template é obrigatório")
    private Long templateId;
}
