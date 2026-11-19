package br.com.docflow.presentation.dto;

import br.com.docflow.domain.model.TemplateContrato;

/** Template de contrato na API. Separado do domínio para o JSON não mudar sozinho quando o modelo mudar. */
public record TemplateContratoResponse(
        Long id,
        String nome,
        String tipoServico,
        String tipoEvento,
        String descricao,
        String versao,
        String cidadeAssinatura,
        String tituloDocumento,
        String subtituloDocumento,
        String clausulaObjeto,
        String clausulaServico,
        String clausulaVisaoArtistica,
        String clausulaPagamento,
        String clausulaDeslocamento,
        String clausulaCancelamentoContratada,
        String clausulaCancelamentoContratante,
        String clausulaObrigacoesContratada,
        String clausulaObrigacoesContratante,
        String clausulaDireitosImagem,
        String clausulaDisposicoesGerais,
        String clausulasConfig,
        String contratadaNome,
        String contratadaDocumento,
        String contratadaEmail,
        String contratadaTelefone
) {

    public static TemplateContratoResponse from(TemplateContrato template) {
        return new TemplateContratoResponse(
                template.getId(),
                template.getNome(),
                template.getTipoServico() != null ? template.getTipoServico().name() : null,
                template.getTipoEvento(),
                template.getDescricao(),
                template.getVersao(),
                template.getCidadeAssinatura(),
                template.getTituloDocumento(),
                template.getSubtituloDocumento(),
                template.getClausulaObjeto(),
                template.getClausulaServico(),
                template.getClausulaVisaoArtistica(),
                template.getClausulaPagamento(),
                template.getClausulaDeslocamento(),
                template.getClausulaCancelamentoContratada(),
                template.getClausulaCancelamentoContratante(),
                template.getClausulaObrigacoesContratada(),
                template.getClausulaObrigacoesContratante(),
                template.getClausulaDireitosImagem(),
                template.getClausulaDisposicoesGerais(),
                template.getClausulasConfig(),
                template.getContratadaNome(),
                template.getContratadaDocumento(),
                template.getContratadaEmail(),
                template.getContratadaTelefone()
        );
    }
}
