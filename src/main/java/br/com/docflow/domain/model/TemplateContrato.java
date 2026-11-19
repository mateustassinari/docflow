package br.com.docflow.domain.model;

import lombok.Data;

@Data
public class TemplateContrato {
    private Long id;
    private String nome;
    private TipoServico tipoServico;
    private String tipoEvento;
    private String descricao;
    private String versao;
    private String cidadeAssinatura;
    private String tituloDocumento;
    private String subtituloDocumento;
    private String clausulaObjeto;
    private String clausulaServico;
    private String clausulaVisaoArtistica;
    private String clausulaPagamento;
    private String clausulaDeslocamento;
    private String clausulaCancelamentoContratada;
    private String clausulaCancelamentoContratante;
    private String clausulaObrigacoesContratada;
    private String clausulaObrigacoesContratante;
    private String clausulaDireitosImagem;
    private String clausulaDisposicoesGerais;
    private String clausulasConfig;
    private String contratadaNome;
    private String contratadaDocumento;
    private String contratadaEmail;
    private String contratadaTelefone;
}
