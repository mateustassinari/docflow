package br.com.docflow.domain.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceTypeCostDefault {
    private String tipoServico;
    private BigDecimal custoDefault;
}
