package br.com.docflow.infrastructure.external.document;

import br.com.docflow.domain.model.BusinessProfile;
import br.com.docflow.domain.model.Client;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.ContractStatus;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.TipoServico;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class OpenPdfContractDocumentGeneratorTest {

    @Test
    void contractShouldShowTheConfiguredBusinessAsContractor() throws Exception {
        OpenPdfContractDocumentGenerator generator =
                new OpenPdfContractDocumentGenerator(new BusinessProfile("Ateliê Exemplo", "Ana", "/nao/existe.png"));

        String text = text(generator.generatePdf(contract()));

        assertThat(text)
                .as("sem template, a contratada é o negócio configurado")
                .contains("Ateliê Exemplo")
                .contains("Cliente Fictício");
    }

    private Contract contract() {
        Client client = new Client();
        client.setNome("Cliente Fictício");
        client.setCpfCnpj("000.000.000-00");
        Event event = new Event();
        event.setId(1L);
        event.setClient(client);
        event.setTipoServico(TipoServico.PINTURA_AO_VIVO);
        event.setTipoEvento("Casamento");
        event.setData(LocalDate.of(2026, 12, 20));
        event.setLocal("Espaço de eventos");
        event.setValor(new BigDecimal("1500"));
        return Contract.restore(1L, event, null, ContractStatus.RASCUNHO, null, null);
    }

    private String text(byte[] pdf) throws Exception {
        PdfReader reader = new PdfReader(pdf);
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        StringBuilder text = new StringBuilder();
        for (int page = 1; page <= reader.getNumberOfPages(); page++) {
            text.append(extractor.getTextFromPage(page)).append('\n');
        }
        return text.toString();
    }
}
