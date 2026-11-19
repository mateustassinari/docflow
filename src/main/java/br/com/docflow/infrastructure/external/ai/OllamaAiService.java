package br.com.docflow.infrastructure.external.ai;

import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.service.AiAssistantService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador que fala com um modelo rodando localmente via Ollama (http://localhost:11434).
 * 100% local: nenhum dado sai da máquina e não há chave/segredo.
 * Espelha o padrão de {@code AutentiqueSignatureService} (RestTemplateBuilder + @Value).
 */
@SuppressWarnings("null")
@Service
public class OllamaAiService implements AiAssistantService {

    private final boolean enabled;
    private final String baseUrl;
    private final String model;
    private final int numPredict;
    private final int numThread;
    private final boolean think;
    private final int numCtx;
    private final double temperature;
    private final RestTemplate restTemplate;

    public OllamaAiService(
            @Value("${ai.enabled:true}") boolean enabled,
            @Value("${ai.ollama.url:http://localhost:11434}") String baseUrl,
            @Value("${ai.ollama.model:qwen2.5:3b}") String model,
            @Value("${ai.ollama.timeout-seconds:300}") long timeoutSeconds,
            @Value("${ai.ollama.num-predict:500}") int numPredict,
            @Value("${ai.ollama.num-thread:6}") int numThread,
            @Value("${ai.ollama.think:false}") boolean think,
            @Value("${ai.ollama.num-ctx:8192}") int numCtx,
            @Value("${ai.ollama.temperature:0.6}") double temperature,
            RestTemplateBuilder restTemplateBuilder
    ) {
        this.enabled = enabled;
        this.baseUrl = baseUrl;
        this.model = model;
        this.numPredict = numPredict;
        this.numThread = numThread;
        this.think = think;
        this.numCtx = numCtx;
        this.temperature = temperature;
        // Conexão rápida (Ollama é local); leitura folgada, pois o modelo pode levar 10-30s+.
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        ensureEnabled();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("stream", false);
        // num_predict acota o tamanho da resposta; num_thread limita a geração aos P-cores
        // (em CPUs híbridas Intel, usar todos os núcleos torna a geração muito mais lenta,
        // pois cada token espera o E-core mais lento).
        // num_ctx: sem ele o Ollama usa a janela padrão (2-4k tokens) e corta o início do prompt
        // em silêncio, levando junto a persona e parte dos dados. temperature um pouco abaixo
        // do padrão (0.8) reduz invenção sem deixar o texto robótico.
        body.put("options", Map.of(
                "num_predict", numPredict,
                "num_thread", numThread,
                "num_ctx", numCtx,
                "temperature", temperature));
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        // "think" só é enviado quando ligado (modelos com raciocínio, ex.: qwen3). Enviar
        // think=false para modelos sem raciocínio (qwen2.5) causaria erro no Ollama.
        if (think) {
            body.put("think", true);
        }

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/api/chat",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<>() {});
            return extractContent(response.getBody());
        } catch (RestClientException ex) {
            // Ollama parado / inacessível: degrada como 400 (via GlobalExceptionHandler), não 500.
            throw new BusinessException(
                    "AI_UNAVAILABLE",
                    "Assistente de IA indisponível. Verifique se o Ollama está em execução (ollama serve).",
                    ex);
        }
    }

    private String extractContent(Map<String, Object> responseBody) {
        if (responseBody != null && responseBody.get("message") instanceof Map<?, ?> message) {
            Object content = message.get("content");
            if (content != null && !content.toString().isBlank()) {
                return content.toString().trim();
            }
        }
        throw new BusinessException("AI_RESPONSE_INVALID", "Resposta inválida do assistente de IA");
    }

    private void ensureEnabled() {
        if (!enabled) {
            throw new BusinessException("AI_DISABLED", "Assistente de IA está desativado");
        }
    }
}
