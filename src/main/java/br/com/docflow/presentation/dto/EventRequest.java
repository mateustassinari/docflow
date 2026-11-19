package br.com.docflow.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import br.com.docflow.application.usecase.command.EventCommand;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class EventRequest {

    @NotNull(message = "O ID do cliente é obrigatório")
    private Long clienteId;

    private String tipoServico;

    private String dadosServico;

    @NotBlank(message = "O tipo do evento é obrigatório")
    private String tipoEvento;

    @NotNull(message = "A data do evento é obrigatória")
    private LocalDate data;

    private LocalTime horarioInicio;

    private String duracaoEstimada;

    @NotBlank(message = "O local do evento é obrigatório")
    private String local;

    private Integer numeroConvidados;

    private String tecnica;

    private String tamanhoTela;

    private String cenaRetratada;

    private String entregaObra;

    @NotNull(message = "O valor do evento é obrigatório")
    private BigDecimal valor;

    private BigDecimal valorSinal;

    private String formaPagamentoSinal;

    private String formaPagamentoRestante;

    private String observacoes;

    private boolean contratoDispensado;

    public EventCommand toCommand() {
        return new EventCommand(
                clienteId, tipoServico, dadosServico, tipoEvento, data, horarioInicio,
                duracaoEstimada, local, numeroConvidados, tecnica, tamanhoTela, cenaRetratada,
                entregaObra, valor, valorSinal, formaPagamentoSinal, formaPagamentoRestante,
                observacoes, contratoDispensado
        );
    }
}
