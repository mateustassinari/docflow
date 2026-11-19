package br.com.docflow.application.usecase.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateEventPaymentCommand(
        boolean sinalRecebido,
        LocalDate dataRecebimentoSinal,
        boolean restanteRecebido,
        LocalDate dataRecebimentoRestante,
        String observacaoPagamento,
        BigDecimal custoTotal,
        BigDecimal custosExtras
) {
}
