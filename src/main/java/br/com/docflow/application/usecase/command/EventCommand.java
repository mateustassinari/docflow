package br.com.docflow.application.usecase.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Dados de um evento para criação ou edição. A validação de formato fica no DTO da borda
 * (presentation); aqui chegam só os valores que o caso de uso usa.
 */
public record EventCommand(
        Long clienteId,
        String tipoServico,
        String dadosServico,
        String tipoEvento,
        LocalDate data,
        LocalTime horarioInicio,
        String duracaoEstimada,
        String local,
        Integer numeroConvidados,
        String tecnica,
        String tamanhoTela,
        String cenaRetratada,
        String entregaObra,
        BigDecimal valor,
        BigDecimal valorSinal,
        String formaPagamentoSinal,
        String formaPagamentoRestante,
        String observacoes,
        boolean contratoDispensado
) {
}
