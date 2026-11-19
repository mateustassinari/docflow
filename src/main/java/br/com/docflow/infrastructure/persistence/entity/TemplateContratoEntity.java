package br.com.docflow.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "templates_contrato")
public class TemplateContratoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(name = "tipo_servico")
    private String tipoServico;

    @Column(name = "tipo_evento")
    private String tipoEvento;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private String versao;

    @Column(name = "cidade_assinatura")
    private String cidadeAssinatura;

    @Column(name = "titulo_documento")
    private String tituloDocumento;

    @Column(name = "subtitulo_documento")
    private String subtituloDocumento;

    @Column(name = "clausula_objeto", columnDefinition = "TEXT")
    private String clausulaObjeto;

    @Column(name = "clausula_servico", columnDefinition = "TEXT")
    private String clausulaServico;

    @Column(name = "clausula_visao_artistica", columnDefinition = "TEXT")
    private String clausulaVisaoArtistica;

    @Column(name = "clausula_pagamento", columnDefinition = "TEXT")
    private String clausulaPagamento;

    @Column(name = "clausula_deslocamento", columnDefinition = "TEXT")
    private String clausulaDeslocamento;

    @Column(name = "clausula_cancelamento_contratada", columnDefinition = "TEXT")
    private String clausulaCancelamentoContratada;

    @Column(name = "clausula_cancelamento_contratante", columnDefinition = "TEXT")
    private String clausulaCancelamentoContratante;

    @Column(name = "clausula_obrigacoes_contratada", columnDefinition = "TEXT")
    private String clausulaObrigacoesContratada;

    @Column(name = "clausula_obrigacoes_contratante", columnDefinition = "TEXT")
    private String clausulaObrigacoesContratante;

    @Column(name = "clausula_direitos_imagem", columnDefinition = "TEXT")
    private String clausulaDireitosImagem;

    @Column(name = "clausula_disposicoes_gerais", columnDefinition = "TEXT")
    private String clausulaDisposicoesGerais;

    @Column(name = "clausulas_config", columnDefinition = "TEXT")
    private String clausulasConfig;

    @Column(name = "contratada_nome")
    private String contratadaNome;

    @Column(name = "contratada_documento")
    private String contratadaDocumento;

    @Column(name = "contratada_email")
    private String contratadaEmail;

    @Column(name = "contratada_telefone")
    private String contratadaTelefone;
}
