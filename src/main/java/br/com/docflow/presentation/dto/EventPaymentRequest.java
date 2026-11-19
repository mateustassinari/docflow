package br.com.docflow.presentation.dto;

import br.com.docflow.application.usecase.command.UpdateEventPaymentCommand;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EventPaymentRequest {

    private boolean sinalRecebido;
    private LocalDate dataRecebimentoSinal;
    private boolean restanteRecebido;
    private LocalDate dataRecebimentoRestante;
    private String observacaoPagamento;
    private BigDecimal custoTotal;
    private BigDecimal custosExtras;

    public UpdateEventPaymentCommand toCommand() {
        return new UpdateEventPaymentCommand(
                sinalRecebido, dataRecebimentoSinal, restanteRecebido, dataRecebimentoRestante,
                observacaoPagamento, custoTotal, custosExtras
        );
    }
}
