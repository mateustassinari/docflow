package br.com.docflow.presentation.dto;

import br.com.docflow.domain.model.ServiceTypeCostDefault;
import java.math.BigDecimal;

/** Custo padrão por tipo de serviço na API. */
public record ServiceTypeCostDefaultResponse(
        String tipoServico,
        BigDecimal custoDefault
) {

    public static ServiceTypeCostDefaultResponse from(ServiceTypeCostDefault costDefault) {
        return new ServiceTypeCostDefaultResponse(
                costDefault.getTipoServico(),
                costDefault.getCustoDefault()
        );
    }
}
