package br.com.docflow.infrastructure.external.signature;

import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.model.AdditionalSigner;
import br.com.docflow.domain.model.Client;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.TipoServico;
import br.com.docflow.domain.service.SignatureService;
import tools.jackson.databind.ObjectMapper;
import org.openpdf.text.pdf.PdfReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("null")
@Service
public class AutentiqueSignatureService implements SignatureService {

    private final String apiUrl;
    private final String apiToken;
    private final String signaturePositionX;
    private final String signaturePositionY;
    private final boolean accountAutoSignEnabled;
    private final String accountEmail;
    private final boolean sandboxEnabled;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AutentiqueSignatureService(
            @Value("${autentique.api.url:}") String apiUrl,
            @Value("${autentique.api.token:}") String apiToken,
            @Value("${autentique.signature.position.x:18.0}") String signaturePositionX,
            @Value("${autentique.signature.position.y:82.0}") String signaturePositionY,
            @Value("${autentique.account.auto-sign.enabled:false}") boolean accountAutoSignEnabled,
            @Value("${autentique.account.email:}") String accountEmail,
            @Value("${autentique.sandbox.enabled:false}") boolean sandboxEnabled,
            @Value("${autentique.api.timeout-seconds:15}") long timeoutSeconds,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.apiUrl = apiUrl;
        this.apiToken = apiToken;
        this.signaturePositionX = signaturePositionX;
        this.signaturePositionY = signaturePositionY;
        this.accountAutoSignEnabled = accountAutoSignEnabled;
        this.accountEmail = accountEmail;
        this.sandboxEnabled = sandboxEnabled;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = restClientBuilder.requestFactory(requestFactory).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String sendForSignature(Contract contract, byte[] pdfBytes, List<AdditionalSigner> additionalSigners) throws Exception {
        ensureConfigured();

        String query = "mutation CreateDocumentMutation($document: DocumentInput!, $signers: [SignerInput!]!, $file: Upload!) { "
                + "createDocument(" + sandboxArgument() + "document: $document, signers: $signers, file: $file) { id } }";

        Map<String, Object> variables = Map.of(
                "document", Map.of("name", buildDocumentName(contract)),
                "signers", buildSigners(contract, pdfBytes, additionalSigners)
        );

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("operations", objectMapper.writeValueAsString(Map.of(
                "query", query,
                "variables", variables
        )));
        body.add("map", objectMapper.writeValueAsString(Map.of("file", List.of("variables.file"))));
        body.add("file", pdfResource(pdfBytes));

        Map<String, Object> response = post(MediaType.MULTIPART_FORM_DATA, body);
        ensureNoGraphqlErrors(response, "AUTENTIQUE_CREATE_DOCUMENT_FAILED");
        Map<String, Object> createDocument = readNestedMap(response, "data", "createDocument");
        Object id = createDocument.get("id");
        if (id == null) {
            throw new BusinessException("AUTENTIQUE_RESPONSE_INVALID", "Autentique nao retornou o ID do documento");
        }

        String documentId = id.toString();
        if (accountAutoSignEnabled) {
            signWithApiAccount(documentId);
        }

        return documentId;
    }

    @Override
    public boolean isContractSigned(String autentiqueId) throws Exception {
        ensureConfigured();

        Map<String, Object> document = fetchDocument(autentiqueId, "signatures { action { name } signed { created_at } }");
        Object rawSignatures = document.get("signatures");
        if (!(rawSignatures instanceof List<?> signatures) || signatures.isEmpty()) {
            return false;
        }

        for (Object rawSignature : signatures) {
            if (!(rawSignature instanceof Map<?, ?> signature)) {
                continue;
            }
            Object rawAction = signature.get("action");
            if (rawAction instanceof Map<?, ?> action && "SIGN".equals(action.get("name")) && signature.get("signed") == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public byte[] downloadSignedPdf(String autentiqueId) throws Exception {
        ensureConfigured();

        Map<String, Object> document = fetchDocument(autentiqueId, "files { signed }");
        Object rawFiles = document.get("files");
        if (!(rawFiles instanceof Map<?, ?> files) || files.get("signed") == null) {
            throw new BusinessException("AUTENTIQUE_SIGNED_FILE_MISSING", "PDF assinado ainda nao esta disponivel no Autentique");
        }

        String signedPdfUrl = files.get("signed").toString();
        if (signedPdfUrl.isBlank()) {
            throw new BusinessException("AUTENTIQUE_SIGNED_FILE_MISSING", "PDF assinado ainda nao esta disponivel no Autentique");
        }

        byte[] body = restClient.get().uri(signedPdfUrl).retrieve().body(byte[].class);
        if (body == null || body.length == 0) {
            throw new BusinessException("AUTENTIQUE_SIGNED_FILE_EMPTY", "Autentique retornou um PDF assinado vazio");
        }

        return body;
    }

    private ByteArrayResource pdfResource(byte[] pdfBytes) {
        return new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return "contrato.pdf";
            }
        };
    }

    private List<Map<String, Object>> buildSigners(Contract contract, byte[] pdfBytes, List<AdditionalSigner> additionalSigners) {
        List<Map<String, Object>> signers = new ArrayList<>();
        signers.add(Map.of(
                "email", contract.getEvent().getClient().getEmail(),
                "action", "SIGN",
                "positions", buildSignaturePositions(pdfBytes)
        ));

        addAdditionalSigners(signers, additionalSigners);

        if (accountAutoSignEnabled) {
            ensureAccountAutoSignConfigured();
            signers.add(Map.of(
                    "email", accountEmail,
                    "action", "SIGN"
            ));
        }

        return signers;
    }

    private void addAdditionalSigners(List<Map<String, Object>> signers, List<AdditionalSigner> additionalSigners) {
        if (additionalSigners == null || additionalSigners.isEmpty()) {
            return;
        }

        Set<String> registeredEmails = new HashSet<>(signers.stream()
                .map(signer -> String.valueOf(signer.get("email")).trim().toLowerCase())
                .toList());

        for (AdditionalSigner signer : additionalSigners) {
            if (signer == null || signer.getEmail() == null || signer.getEmail().isBlank()) {
                continue;
            }

            String email = signer.getEmail().trim();
            if (registeredEmails.contains(email.toLowerCase())) {
                continue;
            }

            signers.add(Map.of(
                    "email", email,
                    "action", "SIGN"
            ));
            registeredEmails.add(email.toLowerCase());
        }
    }

    private String buildDocumentName(Contract contract) {
        Event event = contract != null ? contract.getEvent() : null;
        Client client = event != null ? event.getClient() : null;

        return String.join(" - ",
                "Contrato",
                serviceLabel(event != null ? event.getTipoServico() : null),
                valueOrDefault(event != null ? event.getTipoEvento() : null, "Evento"),
                valueOrDefault(client != null ? client.getNome() : null, "Cliente")
        );
    }

    private String serviceLabel(TipoServico tipoServico) {
        if (tipoServico == null) {
            return "Servico";
        }

        return switch (tipoServico) {
            case PINTURA_AO_VIVO -> "Pintura ao Vivo";
            case AQUARELA_AO_VIVO -> "Aquarela ao Vivo";
            case ENCOMENDA_ARTISTICA -> "Encomenda Artistica";
            case IDENTIDADE_VISUAL -> "Identidade Visual";
        };
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private List<Map<String, Object>> buildSignaturePositions(byte[] pdfBytes) {
        return List.of(Map.of(
                "x", signaturePositionX,
                "y", signaturePositionY,
                "z", countPdfPages(pdfBytes),
                "element", "SIGNATURE"
        ));
    }

    private int countPdfPages(byte[] pdfBytes) {
        try {
            PdfReader reader = new PdfReader(pdfBytes);
            int pages = reader.getNumberOfPages();
            reader.close();
            return Math.max(pages, 1);
        } catch (Exception e) {
            return 1;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readNestedMap(Map<?, ?> root, String firstKey, String secondKey) {
        if (root == null || !(root.get(firstKey) instanceof Map<?, ?> first) || !(first.get(secondKey) instanceof Map<?, ?> second)) {
            throw new BusinessException("AUTENTIQUE_RESPONSE_INVALID", "Resposta invalida da Autentique");
        }
        return (Map<String, Object>) second;
    }

    /** POST na API GraphQL do Autentique, autenticado com o token da conta. */
    private Map<String, Object> post(MediaType contentType, Object body) {
        return restClient.post()
                .uri(apiUrl)
                .contentType(contentType)
                .headers(headers -> headers.setBearerAuth(apiToken))
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    private Map<String, Object> fetchDocument(String autentiqueId, String fields) {
        String query = "query DocumentQuery($id: UUID!) { document(id: $id) { " + fields + " } }";
        Map<String, Object> body = Map.of(
                "query", query,
                "variables", Map.of("id", autentiqueId)
        );

        Map<String, Object> response = post(MediaType.APPLICATION_JSON, body);
        ensureNoGraphqlErrors(response, "AUTENTIQUE_FETCH_DOCUMENT_FAILED");
        return readNestedMap(response, "data", "document");
    }

    private void signWithApiAccount(String autentiqueId) {
        String query = "mutation SignDocumentMutation($id: UUID!) { signDocument(id: $id) }";
        Map<String, Object> body = Map.of(
                "query", query,
                "variables", Map.of("id", autentiqueId)
        );

        Map<?, ?> responseBody = post(MediaType.APPLICATION_JSON, body);
        ensureNoGraphqlErrors(responseBody, "AUTENTIQUE_AUTO_SIGN_FAILED");
        if (responseBody == null || !(responseBody.get("data") instanceof Map<?, ?> data) || data.get("signDocument") == null) {
            throw new BusinessException("AUTENTIQUE_AUTO_SIGN_FAILED", "Autentique nao confirmou a assinatura automatica da conta da API");
        }
    }

    private void ensureConfigured() {
        if (apiUrl == null || apiUrl.isBlank()) {
            throw new BusinessException("AUTENTIQUE_URL_MISSING", "URL da Autentique nao configurada");
        }
        if (apiToken == null || apiToken.isBlank()) {
            throw new BusinessException("AUTENTIQUE_TOKEN_MISSING", "Token da Autentique nao configurado");
        }
    }

    private void ensureAccountAutoSignConfigured() {
        if (accountEmail == null || accountEmail.isBlank()) {
            throw new BusinessException("AUTENTIQUE_ACCOUNT_EMAIL_MISSING", "E-mail da conta Autentique nao configurado para assinatura automatica");
        }
    }

    private String sandboxArgument() {
        return sandboxEnabled ? "sandbox: true, " : "";
    }

    private void ensureNoGraphqlErrors(Map<?, ?> responseBody, String code) {
        if (responseBody == null || !(responseBody.get("errors") instanceof List<?> errors) || errors.isEmpty()) {
            return;
        }

        String message = errors.stream()
                .filter(e -> e instanceof Map<?, ?>)
                .map(e -> (Map<?, ?>) e)
                .map(error -> {
                    Object rawMessage = error.get("message");
                    Object rawExtensions = error.get("extensions");
                    if (rawExtensions instanceof Map<?, ?> extensions && extensions.get("code") != null) {
                        return rawMessage + " (" + extensions.get("code") + ")";
                    }
                    return String.valueOf(rawMessage);
                })
                .reduce((first, second) -> first + "; " + second)
                .orElse("Erro desconhecido retornado pela Autentique");

        throw new BusinessException(code, "Autentique retornou erro: " + message);
    }
}
