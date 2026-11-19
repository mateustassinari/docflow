package br.com.docflow.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceTypeCostDefaultRequest {

    @NotNull
    private BigDecimal custoDefault;
}
