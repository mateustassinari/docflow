package br.com.docflow.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClientEntity client;

    @Column(name = "tipo_servico")
    private String tipoServico;

    @Column(name = "dados_servico", columnDefinition = "TEXT")
    private String dadosServico;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "horario_inicio")
    private LocalTime horarioInicio;

    @Column(name = "duracao_estimada")
    private String duracaoEstimada;

    @Column(nullable = false)
    private String local;

    @Column(name = "numero_convidados")
    private Integer numeroConvidados;

    private String tecnica;

    @Column(name = "tamanho_tela")
    private String tamanhoTela;

    @Column(name = "cena_retratada", columnDefinition = "TEXT")
    private String cenaRetratada;

    @Column(name = "entrega_obra")
    private String entregaObra;

    @Column(nullable = false)
    private java.math.BigDecimal valor;

    @Column(name = "valor_sinal")
    private java.math.BigDecimal valorSinal;

    @Column(name = "forma_pagamento_sinal")
    private String formaPagamentoSinal;

    @Column(name = "forma_pagamento_restante")
    private String formaPagamentoRestante;

    private String observacoes;

    @Column(name = "sinal_recebido", nullable = false)
    private boolean sinalRecebido;

    @Column(name = "data_recebimento_sinal")
    private LocalDate dataRecebimentoSinal;

    @Column(name = "restante_recebido", nullable = false)
    private boolean restanteRecebido;

    @Column(name = "data_recebimento_restante")
    private LocalDate dataRecebimentoRestante;

    @Column(name = "observacao_pagamento", columnDefinition = "TEXT")
    private String observacaoPagamento;

    @Column(name = "custo_total")
    private java.math.BigDecimal custoTotal;

    @Column(name = "contrato_dispensado", nullable = false)
    private boolean contratoDispensado;

    @Column(name = "custos_extras")
    private java.math.BigDecimal custosExtras;
}
