package br.com.docflow.infrastructure.external.document;

import br.com.docflow.domain.model.BusinessProfile;
import br.com.docflow.domain.model.Client;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.TemplateContrato;
import br.com.docflow.domain.service.ContractDocumentGenerator;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OpenPdfContractDocumentGenerator implements ContractDocumentGenerator {

    private final BusinessProfile businessProfile;

    public OpenPdfContractDocumentGenerator(BusinessProfile businessProfile) {
        this.businessProfile = businessProfile;
    }

    private static final Locale LOCALE_BR = Locale.of("pt", "BR");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([\\w.]+)\\s*}}");
    private static final Pattern CONDITIONAL_BLOCK_PATTERN = Pattern.compile(
            "\\{\\{#if\\s+([\\w.]+)\\s*==\\s*\"([^\"]*)\"\\s*}}([\\s\\S]*?)\\{\\{/if}}"
    );
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Color INK = new Color(0x2D, 0x25, 0x24);
    private static final Color ROSE = new Color(0xE4, 0xB8, 0xA7);
    private static final Color ROSE_LIGHT = new Color(0xF7, 0xEE, 0xEA);
    private static final Color GOLD = new Color(0xC9, 0x95, 0x54);
    private static final Color IVORY = new Color(0xFB, 0xF8, 0xF4);
    private static final Color PANEL = new Color(0xF8, 0xF1, 0xEC);
    private static final Color BORDER = new Color(0xE8, 0xD5, 0xCB);
    private static final Color MUTED = new Color(0x7F, 0x73, 0x6F);
    private static final Font FONT_BRAND = FontFactory.getFont(FontFactory.TIMES_ROMAN, 13, INK);
    private static final Font FONT_BRAND_ITALIC = FontFactory.getFont(FontFactory.TIMES_ITALIC, 13, GOLD);
    private static final Font FONT_EYEBROW = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, GOLD);
    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.TIMES_ROMAN, 22, INK);
    private static final Font FONT_CLAUSE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, GOLD);
    private static final Font FONT_CLAUSE_INVERTED = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.WHITE);
    private static final Font FONT_BODY = FontFactory.getFont(FontFactory.HELVETICA, 8.8f, INK);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.8f, INK);
    private static final Font FONT_FIELD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.8f, INK);
    private static final Font FONT_SIGN = FontFactory.getFont(FontFactory.HELVETICA, 8, MUTED);
    private static final Font FONT_SIGN_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, INK);
    private static final float SIGNATURE_DATE_Y = 215f;
    private static final float SIGNATURE_TABLE_TOP_Y = 160f;
    private static final float SIGNATURE_RESERVED_TOP_Y = 265f;

    @Override
    public byte[] generatePdf(Contract contract) throws Exception {
        Event event = contract.getEvent();
        Client client = event != null ? event.getClient() : null;
        TemplateContrato template = contract.getTemplate();

        String clienteNome = valueOrDefault(client != null ? client.getNome() : null, "A definir");
        String clienteCpf = valueOrDefault(client != null ? client.getCpfCnpj() : null, "-");
        String clienteEmail = valueOrDefault(client != null ? client.getEmail() : null, "-");
        String clienteTelefone = valueOrDefault(client != null ? client.getTelefone() : null, "-");

        Map<String, String> dadosServico = parseJsonObject(event != null ? event.getDadosServico() : null);
        String tipoServicoLabel = valueOrDefault(serviceLabel(event, template), "A definir");
        String tipoEvento = valueOrDefault(event != null ? event.getTipoEvento() : null, "A definir");
        String localEvento = valueOrDefault(event != null ? event.getLocal() : null, "A definir");
        String dataEvento = event != null && event.getData() != null
                ? event.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "A definir";
        String horarioInicio = event != null && event.getHorarioInicio() != null
                ? event.getHorarioInicio().format(DateTimeFormatter.ofPattern("HH:mm"))
                : "A definir";
        String duracaoEstimada = valueOrDefault(event != null ? event.getDuracaoEstimada() : null, "A definir");
        String numeroConvidados = event != null && event.getNumeroConvidados() != null
                ? event.getNumeroConvidados() + " pessoas"
                : "A definir";
        String tecnica = valueOrDefault(event != null ? event.getTecnica() : null, "A definir");
        String tamanhoTela = valueOrDefault(event != null ? event.getTamanhoTela() : null, "A definir");
        String cenaRetratada = valueOrDefault(event != null ? event.getCenaRetratada() : null, "A definir");
        String entregaObra = valueOrDefault(event != null ? event.getEntregaObra() : null, "A definir");
        String valorTotal = formatCurrency(event != null ? event.getValor() : null);
        String valorSinal = formatCurrency(event != null ? event.getValorSinal() : null);
        String formaPagamentoSinal = valueOrDefault(event != null ? event.getFormaPagamentoSinal() : null, "A definir");
        String formaPagamentoRestante = valueOrDefault(event != null ? event.getFormaPagamentoRestante() : null, "A definir");
        String observacoesEvento = valueOrDefault(event != null ? event.getObservacoes() : null, "");

        String cidadeAssinatura = valueOrDefault(template != null ? template.getCidadeAssinatura() : null, "Sao Paulo");
        String cidadeData = buildCityDate(cidadeAssinatura);

        String tituloDocumento = valueOrDefault(template != null ? template.getTituloDocumento() : null,
                "CONTRATO DE PRESTACAO DE SERVICOS");
        String subtituloDocumento = valueOrDefault(template != null ? template.getSubtituloDocumento() : null,
                "Modelo em PDF gerado automaticamente");

        String clausulaObjeto = valueOrDefault(template != null ? template.getClausulaObjeto() : null,
                "O presente contrato tem como objeto a prestacao do servico artistico descrito neste modelo, conforme dados do evento informados abaixo.");
        String clausulaServico = valueOrDefault(template != null ? template.getClausulaServico() : null,
                "A contratada realizara o servico artistico combinado com base no briefing aprovado, respeitando as caracteristicas do modelo selecionado.");
        String clausulaVisaoArtistica = valueOrDefault(template != null ? template.getClausulaVisaoArtistica() : null,
                "A obra sera desenvolvida de acordo com a linguagem artistica da contratada, considerando seu estilo e processo criativo.");
        String clausulaPagamento = valueOrDefault(template != null ? template.getClausulaPagamento() : null,
                "As condicoes de pagamento, incluindo sinal, parcelas e saldo final, serao definidas entre as partes e vinculadas a este contrato.");
        String clausulaDeslocamento = valueOrDefault(template != null ? template.getClausulaDeslocamento() : null,
                "Para eventos fora da cidade base da contratada, podera haver cobranca adicional de deslocamento, hospedagem e alimentacao.");
        String clausulaDireitosImagem = valueOrDefault(template != null ? template.getClausulaDireitosImagem() : null,
                "A contratada podera utilizar imagens da obra e do processo para portfolio e divulgacao, salvo manifestacao em contrario por escrito.");
        String clausulaDisposicoesGerais = valueOrDefault(template != null ? template.getClausulaDisposicoesGerais() : null,
                "Este contrato possui carater irrevogavel e irretratavel, e somente podera ser alterado mediante acordo escrito entre as partes.");

        List<String> cancelamentoContratada = splitLines(valueOrDefault(template != null ? template.getClausulaCancelamentoContratada() : null,
                "Em caso de cancelamento por impossibilidade da contratada, os valores pagos serao devolvidos integralmente."));
        List<String> cancelamentoContratante = splitLines(valueOrDefault(template != null ? template.getClausulaCancelamentoContratante() : null,
                "Em caso de cancelamento pelo contratante, o sinal pago para reserva da data nao sera devolvido."));
        List<String> obrigacoesContratada = splitLines(valueOrDefault(template != null ? template.getClausulaObrigacoesContratada() : null,
                "Chegar com antecedencia minima de 60 minutos.\nLevar os materiais necessarios para a execucao do servico.\nManter postura profissional durante todo o evento."));
        List<String> obrigacoesContratante = splitLines(valueOrDefault(template != null ? template.getClausulaObrigacoesContratante() : null,
                "Disponibilizar espaco adequado para a execucao do servico.\nCumprir os pagamentos acordados.\nFornecer as informacoes necessarias para a realizacao do trabalho."));

        String contratadaNome = valueOrDefault(template != null ? template.getContratadaNome() : null, businessProfile.businessName());
        String contratadaDocumento = valueOrDefault(template != null ? template.getContratadaDocumento() : null, "-");
        String contratadaEmail = valueOrDefault(template != null ? template.getContratadaEmail() : null, "-");
        String contratadaTelefone = valueOrDefault(template != null ? template.getContratadaTelefone() : null, "-");
        Map<String, String> placeholders = buildPlaceholders(
                clienteNome,
                clienteCpf,
                clienteEmail,
                clienteTelefone,
                tipoServicoLabel,
                event != null && event.getTipoServico() != null ? event.getTipoServico().name() : "",
                tipoEvento,
                dataEvento,
                horarioInicio,
                duracaoEstimada,
                localEvento,
                numeroConvidados,
                tecnica,
                tamanhoTela,
                cenaRetratada,
                entregaObra,
                valorTotal,
                valorSinal,
                formaPagamentoSinal,
                formaPagamentoRestante,
                observacoesEvento,
                cidadeAssinatura,
                cidadeData,
                contratadaNome,
                contratadaDocumento,
                contratadaEmail,
                contratadaTelefone
        );
        addServiceDataPlaceholders(placeholders, dadosServico);

        tituloDocumento = resolvePlaceholders(tituloDocumento, placeholders);
        subtituloDocumento = resolvePlaceholders(subtituloDocumento, placeholders);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 52, 52, 68, 54);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new HeaderFooterEvent(contratadaNome, contratadaDocumento, contratadaEmail, contratadaTelefone));
        document.open();

        addHeader(document, tituloDocumento, subtituloDocumento);
        addParties(document, clienteNome, clienteCpf, clienteEmail, clienteTelefone, contratadaNome, contratadaDocumento, contratadaEmail, contratadaTelefone);
        document.add(separator());

        if (!addConfiguredClauses(document, template, placeholders)) {
            addClauseWithBoldTokens(document, "CLAUSULA 1 - OBJETO", clausulaObjeto, placeholders);
            addEventTable(document, tipoEvento, dataEvento, horarioInicio, duracaoEstimada, localEvento, numeroConvidados);

            addClauseWithBoldTokens(document, "CLAUSULA 2 - SERVICO PRESTADO", clausulaServico, placeholders);
            addArtworkTable(document, tecnica, tamanhoTela, cenaRetratada, entregaObra);
            if (!observacoesEvento.isBlank()) {
                addBodyParagraph(document, observacoesEvento);
            }

            addClauseWithBoldTokens(document, "CLAUSULA 3 - VISAO ARTISTICA", clausulaVisaoArtistica, placeholders);
            addClauseWithBoldTokens(document, "CLAUSULA 4 - VALOR E FORMA DE PAGAMENTO", clausulaPagamento, placeholders);
            addPaymentTable(document, valorTotal, valorSinal, formaPagamentoSinal, formaPagamentoRestante);

            addClauseWithBoldTokens(document, "CLAUSULA 5 - DESLOCAMENTO E HOSPEDAGEM", clausulaDeslocamento, placeholders);

            addClause(document, "CLAUSULA 6 - CANCELAMENTO E DEVOLUCOES", null);
            addSubTitle(document, "Contratada:");
            addBulletListWithBoldTokens(document, cancelamentoContratada, placeholders);
            addSubTitle(document, "Contratante:");
            addBulletListWithBoldTokens(document, cancelamentoContratante, placeholders);

            addClause(document, "CLAUSULA 7 - OBRIGACOES DAS PARTES", null);
            addSubTitle(document, "Contratada:");
            addBulletListWithBoldTokens(document, obrigacoesContratada, placeholders);
            addSubTitle(document, "Contratante:");
            addBulletListWithBoldTokens(document, obrigacoesContratante, placeholders);

            addClauseWithBoldTokens(document, "CLAUSULA 8 - DIREITOS DE IMAGEM E DIVULGACAO", clausulaDireitosImagem, placeholders);
            addClauseWithBoldTokens(document, "CLAUSULA 9 - DISPOSICOES GERAIS", clausulaDisposicoesGerais, placeholders);
        }

        addSignatures(document, writer, cidadeData, clienteNome, clienteCpf, contratadaNome, contratadaDocumento);

        document.close();
        return baos.toByteArray();
    }

    private void addHeader(Document document, String titleText, String subtitleText) throws DocumentException {
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setSpacingAfter(16);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(IVORY);
        cell.setBorderColor(BORDER);
        cell.setBorderWidth(0.6f);
        cell.setPaddingLeft(22);
        cell.setPaddingRight(22);
        cell.setPaddingTop(18);
        cell.setPaddingBottom(18);

        Paragraph brand = brandParagraph(businessProfile.businessName());
        brand.setAlignment(Element.ALIGN_LEFT);
        brand.setSpacingAfter(12);
        cell.addElement(brand);

        Paragraph title = new Paragraph(titleText, FONT_TITLE);
        title.setLeading(25);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(7);
        cell.addElement(title);

        Paragraph subtitle = new Paragraph(subtitleText, FONT_EYEBROW);
        subtitle.setLeading(12);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(subtitle);

        header.addCell(cell);
        document.add(header);
    }

    private void addParties(Document document,
                            String clientName,
                            String clientDocument,
                            String clientEmail,
                            String clientPhone,
                            String contractorName,
                            String contractorDocument,
                            String contractorEmail,
                            String contractorPhone) throws DocumentException {
        Paragraph intro = new Paragraph(
                "Pelo presente instrumento particular, as partes abaixo identificadas celebram o presente contrato:",
                FONT_BODY
        );
        intro.setLeading(13.5f);
        intro.setSpacingAfter(8);
        document.add(intro);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});
        table.setSpacingAfter(8);

        PdfPCell clientCell = buildPartyCell("CONTRATANTE", clientName, clientDocument, clientEmail, clientPhone, "CPF / CNPJ:");
        PdfPCell contractorCell = buildPartyCell("CONTRATADA", contractorName, contractorDocument, contractorEmail, contractorPhone, "CNPJ:");

        table.addCell(clientCell);
        table.addCell(contractorCell);
        document.add(table);

        Paragraph clause = new Paragraph(
                "Tem entre si, justas e contratadas, as cláusulas a seguir:",
                FONT_BODY
        );
        clause.setLeading(13.5f);
        clause.setSpacingAfter(8);
        document.add(clause);
    }

    private PdfPCell buildPartyCell(String title,
                                    String name,
                                    String document,
                                    String email,
                                    String phone,
                                    String documentLabel) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(BORDER);
        cell.setBorderWidth(0.5f);
        cell.setBackgroundColor(PANEL);
        cell.setPadding(11);

        Paragraph label = new Paragraph(title, FONT_CLAUSE);
        label.setSpacingAfter(6);
        cell.addElement(label);
        cell.addElement(buildField("Nome:", name));
        cell.addElement(buildField(documentLabel, document));
        cell.addElement(buildField("E-mail:", email));
        cell.addElement(buildField("Telefone:", phone));
        return cell;
    }

    private Paragraph buildField(String label, String value) {
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(label + " ", FONT_BODY));
        phrase.add(new Chunk(value, FONT_FIELD));
        Paragraph paragraph = new Paragraph(phrase);
        paragraph.setSpacingAfter(3);
        return paragraph;
    }

    private void addClause(Document document, String title, String text) throws DocumentException {
        PdfPTable stripe = new PdfPTable(1);
        stripe.setWidthPercentage(100);
        stripe.setSpacingBefore(10);
        stripe.setSpacingAfter(6);

        PdfPCell cell = new PdfPCell(new Phrase(title, clauseFont()));
        cell.setBackgroundColor(ROSE_LIGHT);
        cell.setBorderWidthLeft(3.5f);
        cell.setBorderColorLeft(ROSE);
        cell.setBorderWidthTop(0);
        cell.setBorderWidthRight(0);
        cell.setBorderWidthBottom(0);
        cell.setPaddingLeft(9);
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        stripe.addCell(cell);
        document.add(stripe);

        if (text != null && !text.isBlank()) {
            addBodyParagraph(document, text);
        }
    }

    private void addClauseWithBoldTokens(Document document,
                                         String title,
                                         String text,
                                         Map<String, String> placeholders) throws DocumentException {
        addClause(document, title, null);
        addBodyParagraphWithBoldTokens(document, text, placeholders);
    }

    private boolean addConfiguredClauses(Document document,
                                         TemplateContrato template,
                                         Map<String, String> placeholders) throws DocumentException {
        if (template == null || template.getClausulasConfig() == null || template.getClausulasConfig().isBlank()) {
            return false;
        }

        List<Map<String, Object>> clauses;
        try {
            clauses = OBJECT_MAPPER.readValue(template.getClausulasConfig(), new TypeReference<>() {});
        } catch (Exception ignored) {
            return false;
        }

        if (clauses == null || clauses.isEmpty()) {
            return false;
        }

        int index = 1;
        for (Map<String, Object> clause : clauses) {
            String title = stringValue(clause.get("titulo"));
            String textBefore = stringValue(clause.get("textoAntes"));
            if (textBefore.isBlank()) {
                textBefore = stringValue(clause.get("texto"));
            }
            List<String> items = listValue(clause.get("itens"));
            String textAfter = stringValue(clause.get("textoDepois"));

            String finalTitle = title.isBlank() ? "CLAUSULA " + index : title;
            addClause(document, resolvePlaceholders(finalTitle, placeholders), null);
            addBodyParagraphWithBoldTokens(document, textBefore, placeholders);
            if (!items.isEmpty()) {
                addBulletListWithBoldTokens(document, items, placeholders);
            }
            if (!textAfter.isBlank()) {
                addBodyParagraphWithBoldTokens(document, textAfter, placeholders);
            }
            index++;
        }

        return true;
    }

    private void addBodyParagraph(Document document, String text) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, FONT_BODY);
        paragraph.setLeading(14.5f);
        paragraph.setSpacingAfter(5);
        document.add(paragraph);
    }

    private void addBodyParagraphWithBoldTokens(Document document,
                                                String text,
                                                Map<String, String> placeholders) throws DocumentException {
        text = resolveConditionals(text, placeholders);
        if (text == null || text.isBlank()) {
            return;
        }

        Paragraph paragraph = new Paragraph();
        paragraph.setLeading(14.5f);
        paragraph.setSpacingAfter(5);
        addTextWithBoldTokens(paragraph, text, placeholders);
        document.add(paragraph);
    }

    private void addEventTable(Document document,
                               String eventType,
                               String eventDate,
                               String startTime,
                               String duration,
                               String eventLocation,
                               String guests) throws DocumentException {
        PdfPTable table = buildKeyValueTable(
                new String[]{"Tipo de Evento", "Data do Evento", "Horario de Inicio", "Duracao Estimada", "Local", "Numero de Convidados"},
                new String[]{eventType, eventDate, startTime, duration, eventLocation, guests}
        );
        document.add(table);
    }

    private void addArtworkTable(Document document,
                                 String technique,
                                 String canvasSize,
                                 String scene,
                                 String delivery) throws DocumentException {
        PdfPTable table = buildKeyValueTable(
                new String[]{"Tecnica", "Tamanho da Tela", "Cena Retratada", "Entrega da Obra"},
                new String[]{technique, canvasSize, scene, delivery}
        );
        document.add(table);
    }

    private void addPaymentTable(Document document,
                                 String totalValue,
                                 String depositValue,
                                 String depositPaymentMethod,
                                 String remainingPaymentMethod) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(78);
        table.setWidths(new int[]{1, 1});
        table.setSpacingBefore(2f);
        table.setSpacingAfter(8f);

        PdfPCell header = new PdfPCell(new Phrase("VALOR TOTAL DO SERVICO", FONT_CLAUSE_INVERTED));
        header.setBackgroundColor(ROSE);
        header.setPadding(7);
        header.setBorderColor(ROSE);
        table.addCell(header);

        PdfPCell depositHeader = new PdfPCell(new Phrase("VALOR DO SINAL", FONT_CLAUSE_INVERTED));
        depositHeader.setBackgroundColor(ROSE);
        depositHeader.setPadding(7);
        depositHeader.setBorderColor(ROSE);
        table.addCell(depositHeader);

        PdfPCell value = new PdfPCell(new Phrase(totalValue, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, INK)));
        value.setPadding(9);
        value.setBorderColor(BORDER);
        value.setBackgroundColor(ROSE_LIGHT);
        table.addCell(value);

        PdfPCell deposit = new PdfPCell(new Phrase(depositValue, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, INK)));
        deposit.setPadding(9);
        deposit.setBorderColor(BORDER);
        deposit.setBackgroundColor(ROSE_LIGHT);
        table.addCell(deposit);

        PdfPCell depositMethodHeader = new PdfPCell(new Phrase("FORMA DE PAGAMENTO DO SINAL", FONT_CLAUSE_INVERTED));
        depositMethodHeader.setBackgroundColor(ROSE);
        depositMethodHeader.setPadding(7);
        depositMethodHeader.setBorderColor(ROSE);
        table.addCell(depositMethodHeader);

        PdfPCell remainingMethodHeader = new PdfPCell(new Phrase("FORMA DE PAGAMENTO RESTANTE", FONT_CLAUSE_INVERTED));
        remainingMethodHeader.setBackgroundColor(ROSE);
        remainingMethodHeader.setPadding(7);
        remainingMethodHeader.setBorderColor(ROSE);
        table.addCell(remainingMethodHeader);

        PdfPCell depositMethod = new PdfPCell(new Phrase(depositPaymentMethod, FONT_FIELD));
        depositMethod.setPadding(9);
        depositMethod.setBorderColor(BORDER);
        depositMethod.setBackgroundColor(ROSE_LIGHT);
        table.addCell(depositMethod);

        PdfPCell remainingMethod = new PdfPCell(new Phrase(remainingPaymentMethod, FONT_FIELD));
        remainingMethod.setPadding(9);
        remainingMethod.setBorderColor(BORDER);
        remainingMethod.setBackgroundColor(ROSE_LIGHT);
        table.addCell(remainingMethod);

        document.add(table);
    }

    private void addSubTitle(Document document, String text) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, FONT_BOLD);
        paragraph.setSpacingBefore(4);
        paragraph.setSpacingAfter(2);
        document.add(paragraph);
    }

    private void addBulletListWithBoldTokens(Document document,
                                             List<String> items,
                                             Map<String, String> placeholders) throws DocumentException {
        for (String item : items) {
            Paragraph paragraph = new Paragraph();
            paragraph.setIndentationLeft(8);
            paragraph.setLeading(13);
            paragraph.add(new Chunk("  •  ", FONT_BODY));
            addTextWithBoldTokens(paragraph, item, placeholders);
            document.add(paragraph);
        }
        Paragraph spacing = new Paragraph(" ");
        spacing.setSpacingAfter(1);
        document.add(spacing);
    }

    private void addTextWithBoldTokens(Paragraph paragraph,
                                       String text,
                                       Map<String, String> placeholders) {
        text = resolveConditionals(text, placeholders);
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        int cursor = 0;

        while (matcher.find()) {
            if (matcher.start() > cursor) {
                paragraph.add(new Chunk(text.substring(cursor, matcher.start()), FONT_BODY));
            }

            String key = matcher.group(1);
            String replacement = placeholders.get(key);
            if (replacement == null) {
                paragraph.add(new Chunk(matcher.group(0), FONT_BODY));
            } else {
                paragraph.add(new Chunk(replacement, FONT_BOLD));
            }
            cursor = matcher.end();
        }

        if (cursor < text.length()) {
            paragraph.add(new Chunk(text.substring(cursor), FONT_BODY));
        }
    }

    private void addSignatures(Document document,
                               PdfWriter writer,
                               String cityDate,
                               String clientName,
                               String clientDocument,
                               String contractorName,
                               String contractorDocument) throws DocumentException {
        if (writer.getVerticalPosition(false) < SIGNATURE_RESERVED_TOP_Y) {
            document.newPage();
        }

        Paragraph location = new Paragraph(cityDate + ".", FONT_BODY);
        location.setAlignment(Element.ALIGN_CENTER);
        ColumnText.showTextAligned(
                writer.getDirectContent(),
                Element.ALIGN_CENTER,
                location,
                PageSize.A4.getWidth() / 2,
                SIGNATURE_DATE_Y,
                0
        );

        PdfPTable signatures = new PdfPTable(2);
        signatures.setWidthPercentage(86);
        signatures.setHorizontalAlignment(Element.ALIGN_CENTER);
        signatures.setWidths(new float[]{1, 1});
        signatures.addCell(buildSignatureCell("Contratante", clientName + "\nDocumento: " + clientDocument, false));
        signatures.addCell(buildSignatureCell("Contratada", contractorName + "\nDocumento: " + contractorDocument, true));

        float tableWidth = PageSize.A4.getWidth() * 0.86f;
        signatures.setTotalWidth(tableWidth);
        signatures.writeSelectedRows(
                0,
                -1,
                (PageSize.A4.getWidth() - tableWidth) / 2,
                SIGNATURE_TABLE_TOP_Y,
                writer.getDirectContent()
        );
    }

    private PdfPCell buildSignatureCell(String title, String name, boolean includeContractorSignature) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingLeft(12);
        cell.setPaddingRight(12);
        cell.setPaddingTop(0);
        cell.setPaddingBottom(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);

        if (includeContractorSignature) {
            Image signature = loadContractorSignature();
            if (signature != null) {
                signature.scaleToFit(86, 32);
                signature.setAlignment(Image.ALIGN_CENTER);
                cell.addElement(signature);
            } else {
                addSignatureSpacer(cell);
            }
        } else {
            addSignatureSpacer(cell);
        }

        LineSeparator line = new LineSeparator(0.6f, 78, INK, Element.ALIGN_CENTER, -1);
        cell.addElement(new Chunk(line));

        Paragraph titleParagraph = new Paragraph(title, FONT_SIGN_BOLD);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(titleParagraph);

        Paragraph nameParagraph = new Paragraph(name, FONT_SIGN);
        nameParagraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(nameParagraph);
        return cell;
    }

    private void addSignatureSpacer(PdfPCell cell) {
        Paragraph spacer = new Paragraph(" ");
        spacer.setLeading(32);
        cell.addElement(spacer);
    }

    /**
     * Marca no topo do contrato: o nome do negócio, com a última palavra em itálico dourado
     * (ex.: "Ateliê *Exemplo*").
     */
    private Paragraph brandParagraph(String businessName) {
        Paragraph brand = new Paragraph();
        int lastSpace = businessName.lastIndexOf(' ');
        if (lastSpace < 0) {
            brand.add(new Chunk(businessName, FONT_BRAND_ITALIC));
        } else {
            brand.add(new Chunk(businessName.substring(0, lastSpace + 1), FONT_BRAND));
            brand.add(new Chunk(businessName.substring(lastSpace + 1), FONT_BRAND_ITALIC));
        }
        return brand;
    }

    private Image loadContractorSignature() {
        if (!businessProfile.hasSignatureImage()) {
            return null;
        }
        try {
             Path path = Path.of(businessProfile.signatureImage());
            if (Files.exists(path)) {
                return Image.getInstance(path.toAbsolutePath().toString());
            }
        } catch (Exception ignored) {
             // Mantem o PDF funcional mesmo se a assinatura nao estiver disponivel.
        }
        
        return null;
    }

    private PdfPTable buildKeyValueTable(String[] labels, String[] values) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{35, 65});
        table.setSpacingAfter(6f);

        for (int i = 0; i < labels.length; i++) {
            PdfPCell labelCell = new PdfPCell(new Phrase(labels[i], FONT_BODY));
            labelCell.setPadding(7);
            labelCell.setBorderColor(BORDER);
            labelCell.setBackgroundColor(PANEL);

            PdfPCell valueCell = new PdfPCell(new Phrase(values[i], FONT_FIELD));
            valueCell.setPadding(7);
            valueCell.setBorderColor(BORDER);
            valueCell.setBackgroundColor(Color.WHITE);

            table.addCell(labelCell);
            table.addCell(valueCell);
        }

        return table;
    }

    private Paragraph separator() {
        LineSeparator separator = new LineSeparator(0.6f, 100, BORDER, Element.ALIGN_CENTER, -2);
        Paragraph paragraph = new Paragraph(new Chunk(separator));
        paragraph.setSpacingAfter(6);
        return paragraph;
    }

    private Font clauseFont() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, GOLD);
    }

    private String buildCityDate(String city) {
        LocalDate today = LocalDate.now();
        String month = today.getMonth().getDisplayName(TextStyle.FULL, LOCALE_BR);
        return city + ", " + today.getDayOfMonth() + " de " + month + " de " + today.getYear();
    }

    private String formatCurrency(Number value) {
        if (value == null) {
            return "A definir";
        }
        return NumberFormat.getCurrencyInstance(LOCALE_BR).format(value);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String serviceLabel(Event event, TemplateContrato template) {
        if (event != null && event.getTipoServico() != null) {
            return switch (event.getTipoServico()) {
                case PINTURA_AO_VIVO -> "Pintura ao Vivo";
                case AQUARELA_AO_VIVO -> "Aquarela ao Vivo";
                case ENCOMENDA_ARTISTICA -> "Encomenda Artistica Personalizada";
                case IDENTIDADE_VISUAL -> "Identidade Visual";
            };
        }

        if (template != null && template.getTipoServico() != null) {
            return switch (template.getTipoServico()) {
                case PINTURA_AO_VIVO -> "Pintura ao Vivo";
                case AQUARELA_AO_VIVO -> "Aquarela ao Vivo";
                case ENCOMENDA_ARTISTICA -> "Encomenda Artistica Personalizada";
                case IDENTIDADE_VISUAL -> "Identidade Visual";
            };
        }

        return null;
    }

    private Map<String, String> parseJsonObject(String value) {
        if (value == null || value.isBlank()) {
            return new LinkedHashMap<>();
        }

        try {
            Map<String, Object> parsed = OBJECT_MAPPER.readValue(value, new TypeReference<>() {});
            Map<String, String> result = new LinkedHashMap<>();
            parsed.forEach((key, item) -> result.put(key, stringValue(item)));
            return result;
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    private void addServiceDataPlaceholders(Map<String, String> placeholders, Map<String, String> serviceData) {
        serviceData.forEach((key, value) -> {
            placeholders.put("dadosServico." + key, value);
        });
    }

    private String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }

    private List<String> listValue(Object value) {
        List<String> items = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                String text = stringValue(item).trim();
                if (!text.isEmpty()) {
                    items.add(text);
                }
            }
        }
        return items;
    }

    private Map<String, String> buildPlaceholders(String clienteNome,
                                                  String clienteCpf,
                                                  String clienteEmail,
                                                  String clienteTelefone,
                                                  String tipoServicoLabel,
                                                  String tipoServico,
                                                  String tipoEvento,
                                                  String dataEvento,
                                                  String horarioInicio,
                                                  String duracaoEstimada,
                                                  String localEvento,
                                                  String numeroConvidados,
                                                  String tecnica,
                                                  String tamanhoTela,
                                                  String cenaRetratada,
                                                  String entregaObra,
                                                  String valorTotal,
                                                  String valorSinal,
                                                  String formaPagamentoSinal,
                                                  String formaPagamentoRestante,
                                                  String observacoesEvento,
                                                  String cidadeAssinatura,
                                                  String cidadeData,
                                                  String contratadaNome,
                                                  String contratadaDocumento,
                                                  String contratadaEmail,
                                                  String contratadaTelefone) {
        Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("cliente.nome", clienteNome);
        placeholders.put("cliente.documento", clienteCpf);
        placeholders.put("cliente.email", clienteEmail);
        placeholders.put("cliente.telefone", clienteTelefone);
        placeholders.put("evento.tipo_servico", tipoServico);
        placeholders.put("evento.tipo_servico_label", tipoServicoLabel);
        placeholders.put("evento.tipo", tipoEvento);
        placeholders.put("evento.data", dataEvento);
        placeholders.put("evento.horario_inicio", horarioInicio);
        placeholders.put("evento.duracao_estimada", duracaoEstimada);
        placeholders.put("evento.local", localEvento);
        placeholders.put("evento.numero_convidados", numeroConvidados);
        placeholders.put("evento.tecnica", tecnica);
        placeholders.put("evento.tamanho_tela", tamanhoTela);
        placeholders.put("evento.cena_retratada", cenaRetratada);
        placeholders.put("evento.entrega_obra", entregaObra);
        placeholders.put("evento.valor", valorTotal);
        placeholders.put("evento.valor_sinal", valorSinal);
        placeholders.put("evento.pagamento_sinal", formaPagamentoSinal);
        placeholders.put("evento.pagamento_restante", formaPagamentoRestante);
        placeholders.put("evento.observacoes", observacoesEvento.isBlank() ? "" : observacoesEvento);
        placeholders.put("template.cidade_assinatura", cidadeAssinatura);
        placeholders.put("template.cidade_data", cidadeData);
        placeholders.put("contratada.nome", contratadaNome);
        placeholders.put("contratada.documento", contratadaDocumento);
        placeholders.put("contratada.email", contratadaEmail);
        placeholders.put("contratada.telefone", contratadaTelefone);
        return placeholders;
    }

    private String resolvePlaceholders(String text, Map<String, String> placeholders) {
        if (text == null || text.isBlank()) {
            return text;
        }

        text = resolveConditionals(text, placeholders);
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = placeholders.getOrDefault(key, matcher.group(0));
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String resolveConditionals(String text, Map<String, String> placeholders) {
        if (text == null || text.isBlank()) {
            return text;
        }

        Matcher matcher = CONDITIONAL_BLOCK_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String expected = matcher.group(2);
            String content = matcher.group(3);
            String actual = placeholders.getOrDefault(key, "");
            String replacement = actual.equalsIgnoreCase(expected) ? content : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private List<String> splitLines(String value) {
        List<String> items = new ArrayList<>();
        for (String line : value.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                items.add(trimmed);
            }
        }
        return items;
    }

    static class HeaderFooterEvent extends PdfPageEventHelper {

        private final String contractorName;
        private final String contractorDocument;
        private final String contractorEmail;
        private final String contractorPhone;

        HeaderFooterEvent(String contractorName,
                          String contractorDocument,
                          String contractorEmail,
                          String contractorPhone) {
            this.contractorName = contractorName;
            this.contractorDocument = contractorDocument;
            this.contractorEmail = contractorEmail;
            this.contractorPhone = contractorPhone;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte content = writer.getDirectContent();
            float width = document.getPageSize().getWidth();
            float bottom = document.bottomMargin() - 20;
            float top = document.getPageSize().getHeight() - document.topMargin() + 18;

            content.setColorStroke(BORDER);
            content.setLineWidth(0.7f);
            content.moveTo(document.leftMargin(), top);
            content.lineTo(width - document.rightMargin(), top);
            content.stroke();

            content.setColorStroke(BORDER);
            content.setLineWidth(0.4f);
            content.moveTo(document.leftMargin(), bottom + 10);
            content.lineTo(width - document.rightMargin(), bottom + 10);
            content.stroke();

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 6.5f, MUTED);
            String footerText = contractorName + "  |  Documento " + contractorDocument + "  |  "
                    + contractorEmail + "  |  " + contractorPhone;
            ColumnText.showTextAligned(content, Element.ALIGN_LEFT, new Phrase(footerText, footerFont),
                    document.leftMargin(), bottom, 0);
            ColumnText.showTextAligned(content, Element.ALIGN_RIGHT,
                    new Phrase("Pagina " + writer.getPageNumber(), footerFont),
                    width - document.rightMargin(), bottom, 0);
        }
    }
}
