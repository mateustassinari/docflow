package br.com.docflow.presentation.controller;

import br.com.docflow.domain.model.Client;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.TemplateContrato;
import br.com.docflow.domain.model.TipoServico;
import br.com.docflow.domain.repository.TemplateContratoRepository;
import br.com.docflow.domain.service.ContractDocumentGenerator;
import br.com.docflow.presentation.dto.TemplateContratoPreviewRequest;
import br.com.docflow.presentation.dto.TemplateContratoRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.List;

@SuppressWarnings("null")
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateContratoController {

    private final TemplateContratoRepository templateRepository;
    private final ContractDocumentGenerator documentGenerator;

    @GetMapping
    public ResponseEntity<List<TemplateContrato>> listAll() {
        return ResponseEntity.ok(templateRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateContrato> findById(@PathVariable Long id) {
        return templateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TemplateContrato> create(@Valid @RequestBody TemplateContratoRequest request) {
        TemplateContrato template = new TemplateContrato();
        applyRequest(template, request);
        return ResponseEntity.ok(templateRepository.save(template));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemplateContrato> update(@PathVariable Long id, @Valid @RequestBody TemplateContratoRequest request) {
        TemplateContrato template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TEMPLATE_NOT_FOUND", "Template nao encontrado"));
        applyRequest(template, request);
        return ResponseEntity.ok(templateRepository.save(template));
    }

    @PostMapping(value = "/preview", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> preview(@RequestBody TemplateContratoPreviewRequest request) throws Exception {
        TemplateContrato template = new TemplateContrato();
        applyRequest(template, request);

        Client client = new Client();
        client.setNome(defaultIfBlank(request.getClienteNome(), "A definir"));
        client.setCpfCnpj(defaultIfBlank(request.getClienteCpfCnpj(), "A definir"));
        client.setEmail(defaultIfBlank(request.getClienteEmail(), "A definir"));
        client.setTelefone(defaultIfBlank(request.getClienteTelefone(), "A definir"));

        Event event = new Event();
        event.setClient(client);
        event.setTipoServico(parseTipoServico(defaultIfBlank(request.getEventoTipoServico(), request.getTipoServico())));
        event.setDadosServico(request.getEventoDadosServico());
        event.setTipoEvento(defaultIfBlank(request.getEventoTipo(), "A definir"));
        event.setData(request.getEventoData());
        event.setHorarioInicio(parseTime(request.getEventoHorarioInicio()));
        event.setDuracaoEstimada(defaultIfBlank(request.getEventoDuracaoEstimada(), "A definir"));
        event.setLocal(defaultIfBlank(request.getEventoLocal(), "A definir"));
        event.setNumeroConvidados(request.getEventoNumeroConvidados());
        event.setTecnica(defaultIfBlank(request.getEventoTecnica(), "A definir"));
        event.setTamanhoTela(defaultIfBlank(request.getEventoTamanhoTela(), "A definir"));
        event.setCenaRetratada(defaultIfBlank(request.getEventoCenaRetratada(), "A definir"));
        event.setEntregaObra(defaultIfBlank(request.getEventoEntregaObra(), "A definir"));
        event.setValor(request.getEventoValor());
        event.setValorSinal(request.getEventoValorSinal());
        event.setFormaPagamentoSinal(defaultIfBlank(request.getEventoFormaPagamentoSinal(), "A definir"));
        event.setFormaPagamentoRestante(defaultIfBlank(request.getEventoFormaPagamentoRestante(), "A definir"));
        event.setObservacoes(defaultIfBlank(request.getEventoObservacoes(), ""));

        Contract contract = new Contract();
        contract.setTemplate(template);
        contract.setEvent(event);

        byte[] pdf = documentGenerator.generatePdf(contract);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=template-preview.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private void applyRequest(TemplateContrato template, TemplateContratoRequest request) {
        template.setNome(request.getNome());
        template.setTipoServico(parseTipoServico(request.getTipoServico()));
        template.setDescricao(request.getDescricao());
        template.setVersao(request.getVersao());
        template.setCidadeAssinatura(request.getCidadeAssinatura());
        template.setTituloDocumento(request.getTituloDocumento());
        template.setSubtituloDocumento(request.getSubtituloDocumento());
        template.setClausulaObjeto(request.getClausulaObjeto());
        template.setClausulaServico(request.getClausulaServico());
        template.setClausulaVisaoArtistica(request.getClausulaVisaoArtistica());
        template.setClausulaPagamento(request.getClausulaPagamento());
        template.setClausulaDeslocamento(request.getClausulaDeslocamento());
        template.setClausulaCancelamentoContratada(request.getClausulaCancelamentoContratada());
        template.setClausulaCancelamentoContratante(request.getClausulaCancelamentoContratante());
        template.setClausulaObrigacoesContratada(request.getClausulaObrigacoesContratada());
        template.setClausulaObrigacoesContratante(request.getClausulaObrigacoesContratante());
        template.setClausulaDireitosImagem(request.getClausulaDireitosImagem());
        template.setClausulaDisposicoesGerais(request.getClausulaDisposicoesGerais());
        template.setClausulasConfig(request.getClausulasConfig());
        template.setContratadaNome(request.getContratadaNome());
        template.setContratadaDocumento(request.getContratadaDocumento());
        template.setContratadaEmail(request.getContratadaEmail());
        template.setContratadaTelefone(request.getContratadaTelefone());
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalTime.parse(value);
    }

    private TipoServico parseTipoServico(String tipoServico) {
        String value = tipoServico;
        if (value == null || value.isBlank()) {
            return null;
        }

        return switch (value.trim().toUpperCase().replace(' ', '_')) {
            case "AQUARELA_AO_VIVO" -> TipoServico.AQUARELA_AO_VIVO;
            case "ENCOMENDA_ARTISTICA" -> TipoServico.ENCOMENDA_ARTISTICA;
            case "IDENTIDADE_VISUAL" -> TipoServico.IDENTIDADE_VISUAL;
            case "PINTURA_AO_VIVO" -> TipoServico.PINTURA_AO_VIVO;
            default -> null;
        };
    }
}
