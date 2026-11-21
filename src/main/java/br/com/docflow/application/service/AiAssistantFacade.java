package br.com.docflow.application.service;

import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.model.BusinessProfile;
import br.com.docflow.domain.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Orquestra chat e insights: monta o prompt (persona + contexto do sistema) e chama o
 * assistente de IA. Não conhece o provedor concreto (fala pela porta AiAssistantService).
 */
@Slf4j
@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class AiAssistantFacade {

    private static final String PERSONA = """
            Você é o assistente virtual do sistema de gestão de %s, um negócio de arte que realiza \
            pinturas e aquarelas ao vivo em eventos, encomendas artísticas e identidade visual. \
            Você ajuda %s a entender o negócio: finanças, eventos, tarefas, contratos e prazos. \
            Responda SEMPRE em português do Brasil, de forma direta, clara e objetiva. \
            Vá direto à resposta final: NÃO descreva seu raciocínio, NÃO pense em voz alta, \
            NÃO repita a pergunta e NÃO escreva em inglês. \
            Baseie-se EXCLUSIVAMENTE nos dados fornecidos no contexto abaixo. \
            Se a informação não estiver no contexto, diga que não há esse dado registrado. \
            Nunca invente números. Valores monetários estão em reais (R$). \
            Os números, totais e listas do contexto já foram calculados pelo sistema: use-os exatamente \
            como estão, sem somar, contar ou reclassificar por conta própria. \
            Para tarefas atrasadas, de hoje, da semana ou dos próximos dias, responda com a lista do grupo \
            correspondente em TAREFAS e cite as datas; "esta semana" é PARA HOJE mais NO RESTO DESTA SEMANA. \
            O status do contrato e o valor a receber de cada evento estão na linha do evento em PRÓXIMOS EVENTOS.""";

    /** Uma linha com menos letras que isso não é frase (ex.: "!;" quando o modelo degenera). */
    private static final int MIN_LETTERS_PER_INSIGHT = 12;

    private final AiAssistantService ai;
    private final AiContextBuilder contextBuilder;
    private final BusinessProfile businessProfile;

    public boolean isEnabled() {
        return ai.isEnabled();
    }

    public String chat(String question) {
        if (question == null || question.isBlank()) {
            throw new BusinessException("AI_QUESTION_REQUIRED", "A pergunta não pode ser vazia");
        }
        String q = question.trim();
        return contextBuilder.taskDeadlineFocus(q)
                .map(focus -> {
                    log.debug("Pergunta respondida com o contexto focado:\n{}", focus);
                    return ai.generate(buildFocusedSystemPrompt(focus), q);
                })
                .orElseGet(() -> ai.generate(buildSystemPrompt(), q));
    }

    public List<String> insights() {
        String prompt = """
                Com base nos dados do sistema, gere de 3 a 5 insights curtos, práticos e acionáveis \
                para %s sobre o negócio (finanças, pagamentos pendentes, tarefas atrasadas, \
                tendências de serviços ou próximos eventos). \
                Regras de formato: uma frase por insight, começando com um emoji relevante. \
                Não use títulos, numeração, markdown, aspas ou texto introdutório. \
                Retorne apenas as linhas dos insights, uma por linha.""".formatted(businessProfile.addressee());
        String systemPrompt = buildSystemPrompt();
        List<String> insights = generateInsights(systemPrompt, prompt);
        if (insights.isEmpty()) {
            // Modelo pequeno às vezes degenera e devolve só lixo: uma nova tentativa costuma resolver.
            insights = generateInsights(systemPrompt, prompt);
        }
        return insights;
    }

    private List<String> generateInsights(String systemPrompt, String prompt) {
        String raw = ai.generate(systemPrompt, prompt);
        log.debug("Resposta bruta dos insights: {}", raw);
        return parseLines(raw);
    }

    private String buildFocusedSystemPrompt(String focus) {
        // A instrução muda conforme a lista: mencionar "nenhuma" com tarefas na lista fazia o modelo
        // copiar a palavra e responder "nenhuma" mesmo havendo tarefas.
        String instrucao = focus.contains("\n  nenhuma")
                ? "Não há tarefas nesse período: diga isso em uma frase."
                : "Responda listando exatamente as tarefas acima, com as datas, e nenhuma outra.";
        return PERSONA.formatted(businessProfile.businessName(), businessProfile.addressee())
                + "\n\n=== DADOS PARA ESTA PERGUNTA (já filtrados pelo sistema) ===\n" + focus
                + "\n" + instrucao;
    }

    private String buildSystemPrompt() {
        return PERSONA.formatted(businessProfile.businessName(), businessProfile.addressee())
                + "\n\n=== DADOS ATUAIS DO SISTEMA ===\n" + contextBuilder.build();
    }

    private List<String> parseLines(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("\\r?\\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(this::stripBullet)
                .filter(this::looksLikeSentence)
                .limit(5)
                .toList();
    }

    private boolean looksLikeSentence(String line) {
        return line.codePoints().filter(Character::isLetter).count() >= MIN_LETTERS_PER_INSIGHT;
    }

    private String stripBullet(String line) {
        // Remove marcadores de lista comuns: "- ", "* ", "• ", "1. ", "1) "
        return line
                .replaceFirst("^[-*•]\\s+", "")
                .replaceFirst("^\\d+[.)]\\s+", "")
                .trim();
    }
}
