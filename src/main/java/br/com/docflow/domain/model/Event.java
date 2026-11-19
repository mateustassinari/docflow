package br.com.docflow.domain.model;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class Event {
    private Long id;
    private Client client;
    private TipoServico tipoServico;
    private String dadosServico;
    private String tipoEvento; // casamento, corporativo...
    private LocalDate data;
    private LocalTime horarioInicio;
    private String duracaoEstimada;
    private String local;
    private Integer numeroConvidados;
    private String tecnica;
    private String tamanhoTela;
    private String cenaRetratada;
    private String entregaObra;
    private java.math.BigDecimal valor;
    private java.math.BigDecimal valorSinal;
    private String formaPagamentoSinal;
    private String formaPagamentoRestante;
    private String observacoes;
    private boolean sinalRecebido;
    private LocalDate dataRecebimentoSinal;
    private boolean restanteRecebido;
    private LocalDate dataRecebimentoRestante;
    private String observacaoPagamento;
    private java.math.BigDecimal custoTotal;
    private boolean contratoDispensado;
    private java.math.BigDecimal custosExtras;
}
