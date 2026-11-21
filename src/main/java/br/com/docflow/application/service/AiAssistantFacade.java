package br.com.docflow.application.service;

import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.model.BusinessProfile;
import br.com.docflow.domain.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final int MAX_INSIGHTS = 5;

    /** Expressões de prazo que o insight só pode usar se o fato também usar. */
    private static final List<String> TIME_WORDS = List.of("hoje", "amanha", "esta semana", "proxima semana", "este mes", "proximo mes");

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    /** Números com separador de milhar e decimais ("9.100,00", "9100.00") ou simples ("16", "2026"). */
    private static final Pattern NUMBER = Pattern.compile("\\d{1,3}(?:\\.\\d{3})+(?:,\\d+)?|\\d+(?:[.,]\\d+)?");

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
        // O modelo pequeno errava contas e trocava dados entre itens quando recebia tudo junto.
        // Agora o sistema escolhe os fatos (já calculados, os mais importantes primeiro) e o
        // modelo reescreve cada um, na sua própria linha, como um insight.
        List<String> facts = contextBuilder.insightFacts().stream().limit(MAX_INSIGHTS).toList();
        if (facts.isEmpty()) {
            return List.of();
        }
        StringBuilder numbered = new StringBuilder();
        for (int i = 0; i < facts.size(); i++) {
            numbered.append(i + 1).append(". ").append(facts.get(i)).append("\n");
        }
        String systemPrompt = PERSONA.formatted(businessProfile.businessName(), businessProfile.addressee())
                + "\n\nHoje é " + contextBuilder.todayLabel() + ".";
        // O exemplo (com dados fictícios) ensina mais ao modelo pequeno do que as regras sozinhas.
        String prompt = """
                Reescreva cada fato numerado abaixo como um insight para %s: uma frase que diga o que \
                está acontecendo (com quem, quanto e quando, como está no fato) e sugira uma ação.
                Responda com uma linha por fato, começando pelo mesmo número do fato.
                Em cada linha, use somente os números, nomes e datas daquele fato: não junte fatos, \
                não faça contas e não acrescente prazos como "hoje" ou "esta semana" se o fato não disser.
                Comece cada insight com um emoji relevante. Não use títulos, markdown nem texto introdutório.

                Exemplo (dados fictícios):
                Fatos:
                1. Pagamentos em atraso (eventos já realizados e não quitados): 1 — Joana Lima (R$ 500.00).
                2. Eventos futuros sem contrato: 2 — Pedro Alves (10/03/2027), Lia Souza (22/04/2027).
                Insights:
                1. 💸 Joana Lima ainda deve R$ 500,00 de um evento que já aconteceu: vale fazer a cobrança.
                2. 📄 Os eventos de Pedro Alves (10/03/2027) e Lia Souza (22/04/2027) ainda estão sem contrato: gere e envie para assinatura.

                Fatos:
                %s
                Insights:""".formatted(businessProfile.addressee(), numbered);
        List<String> clients = contextBuilder.clientNames();
        List<String> insights = generateInsights(systemPrompt, prompt, facts, clients);
        if (insights.isEmpty()) {
            // Modelo pequeno às vezes degenera e devolve só lixo: uma nova tentativa costuma resolver.
            insights = generateInsights(systemPrompt, prompt, facts, clients);
        }
        return insights;
    }

    private List<String> generateInsights(String systemPrompt, String prompt, List<String> facts, List<String> clients) {
        String raw = ai.generate(systemPrompt, prompt);
        log.debug("Resposta bruta dos insights: {}", raw);
        List<String> lines = Arrays.stream(raw == null ? new String[0] : raw.split("\\r?\\n"))
                .map(String::trim)
                .filter(this::looksLikeSentence)
                .map(this::stripBullet)
                .toList();
        List<String> insights = new ArrayList<>();
        Set<Integer> usados = new HashSet<>();
        for (int pos = 0; pos < lines.size(); pos++) {
            String insight = lines.get(pos);
            int fato = matchingFact(insight, pos, facts, usados, clients);
            if (fato < 0) {
                log.info("Insight descartado por não corresponder a nenhum fato (número, data, prazo ou cliente fora dos fatos): {}", insight);
                continue;
            }
            usados.add(fato);
            insights.add(insight);
        }
        return insights;
    }

    /**
     * Liga o insight ao fato que contém todos os números e datas que ele cita (o modelo às vezes
     * divide ou reordena os fatos, então a posição da linha não basta). Sem número no insight, vale
     * a posição. Número fora de todos os fatos (invenção ou troca de dados), prazo como "hoje" ou
     * cliente que o fato não tem: nenhum fato corresponde e o insight cai. Cada fato rende um insight.
     */
    private int matchingFact(String insight, int position, List<String> facts, Set<Integer> usados, List<String> clients) {
        Set<String> citados = numbers(insight);
        int melhor = -1;
        int melhorComum = -1;
        for (int i = 0; i < facts.size(); i++) {
            if (usados.contains(i) || !sameDeadlineWords(insight, facts.get(i)) || !sameClients(insight, facts.get(i), clients)) {
                continue;
            }
            Set<String> doFato = numbers(facts.get(i));
            if (citados.isEmpty()) {
                if (i == position) {
                    return i;
                }
                continue;
            }
            if (doFato.containsAll(citados) && citados.size() > melhorComum) {
                melhor = i;
                melhorComum = citados.size();
            }
        }
        return melhor;
    }

    /** Cliente citado no insight (pelo nome completo ou pelo primeiro nome) precisa estar no fato. */
    private boolean sameClients(String insight, String fact, List<String> clients) {
        String i = " " + normalize(insight).replaceAll("[^a-z0-9]+", " ") + " ";
        String f = normalize(fact);
        for (String client : clients) {
            String nome = normalize(client);
            String primeiroNome = nome.split("\\s+")[0];
            if (i.contains(" " + primeiroNome + " ") && !f.contains(nome)) {
                return false;
            }
        }
        return true;
    }

    private boolean sameDeadlineWords(String insight, String fact) {
        String i = normalize(insight);
        String f = normalize(fact);
        return TIME_WORDS.stream().noneMatch(w -> i.contains(w) && !f.contains(w));
    }

    /** Números de um texto, normalizados: "R$ 9.100,00", "9100.00" e "9100" viram "9100"; datas viram dia, mês e ano. */
    static Set<String> numbers(String text) {
        Set<String> out = new HashSet<>();
        Matcher m = NUMBER.matcher(text == null ? "" : text);
        while (m.find()) {
            String n = m.group();
            if (n.contains(",") && n.contains(".")) {
                n = n.replace(".", "").replace(",", ".");
            } else if (n.contains(",")) {
                n = n.replace(",", ".");
            } else if (n.matches("\\d{1,3}(\\.\\d{3})+")) {
                n = n.replace(".", "");
            }
            out.add(new BigDecimal(n).stripTrailingZeros().toPlainString());
        }
        return out;
    }

    private String buildFocusedSystemPrompt(String focus) {
        // A instrução muda conforme a lista: mencionar "nenhuma" com tarefas na lista fazia o modelo
        // copiar a palavra e responder "nenhuma" mesmo havendo tarefas.
        boolean pagamentos = focus.contains("PAGAMENTOS EM ATRASO");
        String instrucao = focus.contains("\n  nenhuma")
                ? (pagamentos ? "Não há pagamentos em atraso: diga isso em uma frase." : "Não há tarefas nesse período: diga isso em uma frase.")
                : (pagamentos ? "Responda com exatamente esses clientes e valores, e nenhum outro."
                              : "Responda listando exatamente as tarefas acima, com as datas, e nenhuma outra.");
        return PERSONA.formatted(businessProfile.businessName(), businessProfile.addressee())
                + "\n\n=== DADOS PARA ESTA PERGUNTA (já filtrados pelo sistema) ===\n" + focus
                + "\n" + instrucao;
    }

    private String buildSystemPrompt() {
        return PERSONA.formatted(businessProfile.businessName(), businessProfile.addressee())
                + "\n\n=== DADOS ATUAIS DO SISTEMA ===\n" + contextBuilder.build();
    }

    private boolean looksLikeSentence(String line) {
        return line.codePoints().filter(Character::isLetter).count() >= MIN_LETTERS_PER_INSIGHT;
    }

    private String stripBullet(String line) {
        // Remove marcadores de lista comuns: "- ", "* ", "• ", "1. ", "1) "
        return line
                .replaceFirst("^\\d+[.)]\\s+", "")
                .replaceFirst("^[-*•]\\s+", "")
                .trim();
    }
}
