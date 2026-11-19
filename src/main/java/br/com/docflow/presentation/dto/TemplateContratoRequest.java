package br.com.docflow.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TemplateContratoRequest {

    @NotBlank(message = "O nome do template e obrigatorio")
    private String nome;

    private String tipoServico;

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
