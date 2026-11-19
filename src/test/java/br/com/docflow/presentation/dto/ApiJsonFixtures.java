package br.com.docflow.presentation.dto;

import br.com.docflow.domain.model.Client;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.ServiceTypeCostDefault;
import br.com.docflow.domain.model.TemplateContrato;
import br.com.docflow.domain.model.TipoServico;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Objetos com todos os campos preenchidos (valores distintos), para fixar o JSON da API. */
final class ApiJsonFixtures {

    private ApiJsonFixtures() {
    }

    static Client client() {
        Client client = new Client();
        client.setId(7L);
        client.setNome("Mariana Costa");
        client.setCpfCnpj("111.111.111-11");
        client.setEmail("mariana@exemplo.com");
        client.setTelefone("(11) 90000-0001");
        return client;
    }

    static Event event() {
        Event event = new Event();
        event.setId(10L);
        event.setClient(client());
        event.setTipoServico(TipoServico.PINTURA_AO_VIVO);
        event.setDadosServico("{\"tecnica\":\"oleo\"}");
        event.setTipoEvento("Casamento");
        event.setData(LocalDate.of(2026, 12, 20));
        event.setHorarioInicio(LocalTime.of(16, 30));
        event.setDuracaoEstimada("5 horas");
        event.setLocal("Espaço Jardim");
        event.setNumeroConvidados(180);
        event.setTecnica("Óleo");
        event.setTamanhoTela("60x80");
        event.setCenaRetratada("Cerimônia");
        event.setEntregaObra("No evento");
        event.setValor(new BigDecimal("3500.00"));
        event.setValorSinal(new BigDecimal("1000.50"));
        event.setFormaPagamentoSinal("PIX");
        event.setFormaPagamentoRestante("Cartão");
        event.setObservacoes("Ao ar livre");
        event.setSinalRecebido(true);
        event.setDataRecebimentoSinal(LocalDate.of(2026, 11, 5));
        event.setRestanteRecebido(false);
        event.setDataRecebimentoRestante(null);
        event.setObservacaoPagamento("Restante no dia");
        event.setCustoTotal(new BigDecimal("900"));
        event.setContratoDispensado(false);
        event.setCustosExtras(new BigDecimal("50.25"));
        return event;
    }

    static TemplateContrato template() {
        TemplateContrato t = new TemplateContrato();
        t.setId(3L);
        t.setNome("Template Pintura");
        t.setTipoServico(TipoServico.AQUARELA_AO_VIVO);
        t.setTipoEvento("Casamento");
        t.setDescricao("descricao");
        t.setVersao("2");
        t.setCidadeAssinatura("Sao Paulo");
        t.setTituloDocumento("titulo");
        t.setSubtituloDocumento("subtitulo");
        t.setClausulaObjeto("objeto");
        t.setClausulaServico("servico");
        t.setClausulaVisaoArtistica("visao");
        t.setClausulaPagamento("pagamento");
        t.setClausulaDeslocamento("deslocamento");
        t.setClausulaCancelamentoContratada("cancelamento contratada");
        t.setClausulaCancelamentoContratante("cancelamento contratante");
        t.setClausulaObrigacoesContratada("obrigacoes contratada");
        t.setClausulaObrigacoesContratante("obrigacoes contratante");
        t.setClausulaDireitosImagem("imagem");
        t.setClausulaDisposicoesGerais("gerais");
        t.setClausulasConfig("[]");
        t.setContratadaNome("Atelie Exemplo");
        t.setContratadaDocumento("00.000.000/0001-00");
        t.setContratadaEmail("contato@exemplo.com");
        t.setContratadaTelefone("(11) 90000-0000");
        return t;
    }

    static ServiceTypeCostDefault costDefault() {
        ServiceTypeCostDefault cost = new ServiceTypeCostDefault();
        cost.setTipoServico("PINTURA_AO_VIVO");
        cost.setCustoDefault(new BigDecimal("450.00"));
        return cost;
    }
}
