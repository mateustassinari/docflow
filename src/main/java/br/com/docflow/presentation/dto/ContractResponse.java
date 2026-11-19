package br.com.docflow.presentation.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ContractResponse {
    private Long id;
    private String status;
    private String pdfUrl;
    private String autentiqueId;
    private EventSummary event;
    private TemplateSummary template;

    @Data
    public static class EventSummary {
        private Long id;
        private String tipoServico;
        private String dadosServico;
        private String tipoEvento;
        private LocalDate data;
        private LocalTime horarioInicio;
        private String duracaoEstimada;
        private String local;
        private Integer numeroConvidados;
        private String tecnica;
        private String tamanhoTela;
        private String cenaRetratada;
        private String entregaObra;
        private BigDecimal valor;
        private BigDecimal valorSinal;
        private String formaPagamentoSinal;
        private String formaPagamentoRestante;
        private String observacoes;
        private ClientSummary client;
    }

    @Data
    public static class ClientSummary {
        private Long id;
        private String nome;
    }

    @Data
    public static class TemplateSummary {
        private Long id;
        private String nome;
        private String tipoServico;
        private String tipoEvento;
        private String versao;
    }
}
