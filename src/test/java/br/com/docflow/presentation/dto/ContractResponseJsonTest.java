package br.com.docflow.presentation.dto;

import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.ContractStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/** O caminho do PDF no servidor (pasta de uploads) é detalhe interno e não pode sair na API. */
@JsonTest
class ContractResponseJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldTellWhetherThePdfExistsWithoutExposingTheServerPath() {
        Contract contract = Contract.restore(3L, ApiJsonFixtures.event(), ApiJsonFixtures.template(),
                ContractStatus.RASCUNHO, "/app/uploads/contracts/contract_event_10.pdf", null);

        String json = objectMapper.writeValueAsString(ContractResponseMapper.toResponse(contract));
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.get("hasPdf").asBoolean()).isTrue();
        assertThat(node.has("pdfUrl")).isFalse();
        assertThat(json).doesNotContain("/app/uploads");
    }

    @Test
    void contractWithoutPdfShouldSayItHasNone() {
        Contract contract = Contract.restore(4L, ApiJsonFixtures.event(), ApiJsonFixtures.template(),
                ContractStatus.RASCUNHO, null, null);

        JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(ContractResponseMapper.toResponse(contract)));

        assertThat(node.get("hasPdf").asBoolean()).isFalse();
    }
}
