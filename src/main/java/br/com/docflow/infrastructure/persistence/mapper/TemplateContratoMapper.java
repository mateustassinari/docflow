package br.com.docflow.infrastructure.persistence.mapper;

import br.com.docflow.domain.model.TemplateContrato;
import br.com.docflow.domain.model.TipoServico;
import br.com.docflow.infrastructure.persistence.entity.TemplateContratoEntity;

public class TemplateContratoMapper {

    public static TemplateContratoEntity toEntity(TemplateContrato template) {
        if (template == null) return null;

        TemplateContratoEntity entity = new TemplateContratoEntity();
        entity.setId(template.getId());
        entity.setNome(template.getNome());
        entity.setTipoServico(template.getTipoServico() != null ? template.getTipoServico().name() : null);
        entity.setTipoEvento(template.getTipoEvento());
        entity.setDescricao(template.getDescricao());
        entity.setVersao(template.getVersao());
        entity.setCidadeAssinatura(template.getCidadeAssinatura());
        entity.setTituloDocumento(template.getTituloDocumento());
        entity.setSubtituloDocumento(template.getSubtituloDocumento());
        entity.setClausulaObjeto(template.getClausulaObjeto());
        entity.setClausulaServico(template.getClausulaServico());
        entity.setClausulaVisaoArtistica(template.getClausulaVisaoArtistica());
        entity.setClausulaPagamento(template.getClausulaPagamento());
        entity.setClausulaDeslocamento(template.getClausulaDeslocamento());
        entity.setClausulaCancelamentoContratada(template.getClausulaCancelamentoContratada());
        entity.setClausulaCancelamentoContratante(template.getClausulaCancelamentoContratante());
        entity.setClausulaObrigacoesContratada(template.getClausulaObrigacoesContratada());
        entity.setClausulaObrigacoesContratante(template.getClausulaObrigacoesContratante());
        entity.setClausulaDireitosImagem(template.getClausulaDireitosImagem());
        entity.setClausulaDisposicoesGerais(template.getClausulaDisposicoesGerais());
        entity.setClausulasConfig(template.getClausulasConfig());
        entity.setContratadaNome(template.getContratadaNome());
        entity.setContratadaDocumento(template.getContratadaDocumento());
        entity.setContratadaEmail(template.getContratadaEmail());
        entity.setContratadaTelefone(template.getContratadaTelefone());
        return entity;
    }

    public static TemplateContrato toDomain(TemplateContratoEntity entity) {
        if (entity == null) return null;

        TemplateContrato template = new TemplateContrato();
        template.setId(entity.getId());
        template.setNome(entity.getNome());
        template.setTipoServico(parseTipoServico(entity.getTipoServico()));
        template.setTipoEvento(entity.getTipoEvento());
        template.setDescricao(entity.getDescricao());
        template.setVersao(entity.getVersao());
        template.setCidadeAssinatura(entity.getCidadeAssinatura());
        template.setTituloDocumento(entity.getTituloDocumento());
        template.setSubtituloDocumento(entity.getSubtituloDocumento());
        template.setClausulaObjeto(entity.getClausulaObjeto());
        template.setClausulaServico(entity.getClausulaServico());
        template.setClausulaVisaoArtistica(entity.getClausulaVisaoArtistica());
        template.setClausulaPagamento(entity.getClausulaPagamento());
        template.setClausulaDeslocamento(entity.getClausulaDeslocamento());
        template.setClausulaCancelamentoContratada(entity.getClausulaCancelamentoContratada());
        template.setClausulaCancelamentoContratante(entity.getClausulaCancelamentoContratante());
        template.setClausulaObrigacoesContratada(entity.getClausulaObrigacoesContratada());
        template.setClausulaObrigacoesContratante(entity.getClausulaObrigacoesContratante());
        template.setClausulaDireitosImagem(entity.getClausulaDireitosImagem());
        template.setClausulaDisposicoesGerais(entity.getClausulaDisposicoesGerais());
        template.setClausulasConfig(entity.getClausulasConfig());
        template.setContratadaNome(entity.getContratadaNome());
        template.setContratadaDocumento(entity.getContratadaDocumento());
        template.setContratadaEmail(entity.getContratadaEmail());
        template.setContratadaTelefone(entity.getContratadaTelefone());
        return template;
    }

    private static TipoServico parseTipoServico(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return TipoServico.valueOf(value);
    }
}
