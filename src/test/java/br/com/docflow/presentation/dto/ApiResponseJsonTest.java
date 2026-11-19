package br.com.docflow.presentation.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

/**
 * Fixa o contrato JSON de eventos, clientes, templates e custos padrão (o que o frontend lê),
 * com o ObjectMapper configurado pelo Spring Boot. Os valores esperados são o JSON que a API
 * devolvia quando serializava o domínio direto; os DTOs de resposta precisam manter exatamente o mesmo.
 */
@JsonTest
class ApiResponseJsonTest {

    static final String EVENT_JSON = """
            {
              "id" : 10,
              "client" : {
                "id" : 7,
                "nome" : "Mariana Costa",
                "cpfCnpj" : "111.111.111-11",
                "email" : "mariana@exemplo.com",
                "telefone" : "(11) 90000-0001"
              },
              "tipoServico" : "PINTURA_AO_VIVO",
              "dadosServico" : "{\\"tecnica\\":\\"oleo\\"}",
              "tipoEvento" : "Casamento",
              "data" : "2026-12-20",
              "horarioInicio" : "16:30:00",
              "duracaoEstimada" : "5 horas",
              "local" : "Espaço Jardim",
              "numeroConvidados" : 180,
              "tecnica" : "Óleo",
              "tamanhoTela" : "60x80",
              "cenaRetratada" : "Cerimônia",
              "entregaObra" : "No evento",
              "valor" : 3500.00,
              "valorSinal" : 1000.50,
              "formaPagamentoSinal" : "PIX",
              "formaPagamentoRestante" : "Cartão",
              "observacoes" : "Ao ar livre",
              "sinalRecebido" : true,
              "dataRecebimentoSinal" : "2026-11-05",
              "restanteRecebido" : false,
              "dataRecebimentoRestante" : null,
              "observacaoPagamento" : "Restante no dia",
              "custoTotal" : 900,
              "contratoDispensado" : false,
              "custosExtras" : 50.25
            }
            """;

    static final String CLIENT_JSON = """
            {
              "id" : 7,
              "nome" : "Mariana Costa",
              "cpfCnpj" : "111.111.111-11",
              "email" : "mariana@exemplo.com",
              "telefone" : "(11) 90000-0001"
            }
            """;

    static final String TEMPLATE_JSON = """
            {
              "id" : 3,
              "nome" : "Template Pintura",
              "tipoServico" : "AQUARELA_AO_VIVO",
              "tipoEvento" : "Casamento",
              "descricao" : "descricao",
              "versao" : "2",
              "cidadeAssinatura" : "Sao Paulo",
              "tituloDocumento" : "titulo",
              "subtituloDocumento" : "subtitulo",
              "clausulaObjeto" : "objeto",
              "clausulaServico" : "servico",
              "clausulaVisaoArtistica" : "visao",
              "clausulaPagamento" : "pagamento",
              "clausulaDeslocamento" : "deslocamento",
              "clausulaCancelamentoContratada" : "cancelamento contratada",
              "clausulaCancelamentoContratante" : "cancelamento contratante",
              "clausulaObrigacoesContratada" : "obrigacoes contratada",
              "clausulaObrigacoesContratante" : "obrigacoes contratante",
              "clausulaDireitosImagem" : "imagem",
              "clausulaDisposicoesGerais" : "gerais",
              "clausulasConfig" : "[]",
              "contratadaNome" : "Atelie Exemplo",
              "contratadaDocumento" : "00.000.000/0001-00",
              "contratadaEmail" : "contato@exemplo.com",
              "contratadaTelefone" : "(11) 90000-0000"
            }
            """;

    static final String COST_DEFAULT_JSON = """
            {
              "tipoServico" : "PINTURA_AO_VIVO",
              "custoDefault" : 450.00
            }
            """;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void eventShouldKeepTheJsonContract() throws Exception {
        assertJson(EVENT_JSON, EventResponse.from(ApiJsonFixtures.event()));
    }

    @Test
    void clientShouldKeepTheJsonContract() throws Exception {
        assertJson(CLIENT_JSON, ClientResponse.from(ApiJsonFixtures.client()));
    }

    @Test
    void templateShouldKeepTheJsonContract() throws Exception {
        assertJson(TEMPLATE_JSON, TemplateContratoResponse.from(ApiJsonFixtures.template()));
    }

    @Test
    void costDefaultShouldKeepTheJsonContract() throws Exception {
        assertJson(COST_DEFAULT_JSON, ServiceTypeCostDefaultResponse.from(ApiJsonFixtures.costDefault()));
    }

    private void assertJson(String expected, Object response) throws Exception {
        JSONAssert.assertEquals(expected, objectMapper.writeValueAsString(response), JSONCompareMode.STRICT);
    }
}
