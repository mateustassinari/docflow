package br.com.docflow.application.service;

import br.com.docflow.domain.model.BusinessProfile;
import br.com.docflow.domain.service.AiAssistantService;
import java.util.Optional;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiAssistantFacadeTest {

    private final AiAssistantService ai = mock(AiAssistantService.class);
    private final AiContextBuilder contextBuilder = mock(AiContextBuilder.class);
    private final AiAssistantFacade facade = new AiAssistantFacade(ai, contextBuilder, new BusinessProfile("Ateliê Exemplo", "Ana", ""));

    @Test
    void shouldDropGarbageLinesWhenModelDegenerates() {
        when(contextBuilder.insightFacts()).thenReturn(List.of("Pagamentos em atraso: 6 — Ana.", "Tarefas para hoje: 2."));
        when(ai.generate(anyString(), anyString())).thenReturn("""
                !;
                1. Ana tem 6 eventos em atraso e deve priorizar a cobrança.
                ;
                2. - 💰 Duas tarefas vencem hoje.""");

        assertThat(facade.insights())
                .as("linhas sem frase (ex.: \"!;\") não podem aparecer como insight")
                .containsExactly("Ana tem 6 eventos em atraso e deve priorizar a cobrança.", "💰 Duas tarefas vencem hoje.");
        verify(ai, times(1)).generate(anyString(), anyString());
    }

    @Test
    void shouldRetryOnceWhenEveryLineIsGarbage() {
        when(contextBuilder.insightFacts()).thenReturn(List.of("Tarefas para hoje: 2."));
        when(ai.generate(anyString(), anyString()))
                .thenReturn("!;\n;\n!;")
                .thenReturn("1. 📅 Duas tarefas vencem hoje: confira a agenda.");

        assertThat(facade.insights()).containsExactly("📅 Duas tarefas vencem hoje: confira a agenda.");
        verify(ai, times(2)).generate(anyString(), anyString());
    }

    @Test
    void shouldReturnEmptyWhenRetryIsAlsoGarbage() {
        when(contextBuilder.insightFacts()).thenReturn(List.of("Eventos futuros: 4."));
        when(ai.generate(anyString(), anyString())).thenReturn("!;\n;");

        assertThat(facade.insights())
                .as("sem insight válido, a tela mostra \"Nenhum insight gerado\" em vez de lixo")
                .isEmpty();
        verify(ai, times(2)).generate(anyString(), anyString());
    }

    @Test
    void withoutFactsThereIsNothingToAskTheModel() {
        when(contextBuilder.insightFacts()).thenReturn(List.of());

        assertThat(facade.insights()).isEmpty();
        verify(ai, never()).generate(anyString(), anyString());
    }

    @Test
    void eachInsightMayOnlyCiteNumbersFromItsOwnFact() {
        when(contextBuilder.insightFacts()).thenReturn(List.of(
                "Eventos futuros sem contrato: 2 — Mariana Costa (16/12/2026), Rafael e Júlia Souza (20/01/2027).",
                "Total a receber dos 4 eventos futuros, somados: R$ 9100.00."));
        when(ai.generate(anyString(), anyString())).thenReturn("""
                1. 📄 Mariana Costa (16/12) e Rafael e Júlia Souza (20/01) ainda estão sem contrato.
                2. 💸 Há R$ 9.100,00 a receber da Mariana Costa em 16/12/2026.
                1. 📄 Repetido para o mesmo fato.
                3. 📊 O custo médio por evento é de R$ 3166,67.""");

        assertThat(facade.insights())
                .as("a data 16/12 do fato 1 citada no fato 2 é troca de dados; fato 3 não existe; fato repetido vale uma vez")
                .containsExactly("📄 Mariana Costa (16/12) e Rafael e Júlia Souza (20/01) ainda estão sem contrato.");
    }

    @Test
    void insightsMayNotInventADeadlineTheFactDoesNotHave() {
        when(contextBuilder.insightFacts()).thenReturn(List.of(
                "Tarefas nos próximos 7 dias: 1 — Atualizar portfólio no Instagram (prazo 23/11/2026).",
                "Tarefas para hoje: 1 — Compra de material (Mariana Costa, prazo 21/11/2026)."));
        when(ai.generate(anyString(), anyString())).thenReturn("""
                1. 📱 Atualizar o portfólio no Instagram: comece as postagens hoje.
                2. 🛍️ A compra de material da Mariana Costa vence hoje: garanta os itens.""");

        assertThat(facade.insights())
                .as("o fato 1 não fala em hoje; o fato 2 fala")
                .containsExactly("🛍️ A compra de material da Mariana Costa vence hoje: garanta os itens.");
    }

    @Test
    void unnumberedLinesShouldMatchTheFactsByOrder() {
        when(contextBuilder.insightFacts()).thenReturn(List.of(
                "Pagamentos em atraso (eventos já realizados e não quitados): 1 — Horizonte (R$ 2700.00).",
                "Eventos futuros sem contrato: 1 — Rafael (20/01/2027)."));
        when(ai.generate(anyString(), anyString())).thenReturn("""
                💰 A Horizonte ainda não quitou R$ 2700,00 de um evento realizado: vale cobrar.

                📄 O evento do Rafael em 20/01/2027 está sem contrato: gere e envie.""");

        assertThat(facade.insights()).containsExactly(
                "💰 A Horizonte ainda não quitou R$ 2700,00 de um evento realizado: vale cobrar.",
                "📄 O evento do Rafael em 20/01/2027 está sem contrato: gere e envie.");
    }

    @Test
    void insightsMayNotAttributeAFactToTheWrongClient() {
        when(contextBuilder.clientNames()).thenReturn(List.of("Mariana Costa", "Carla Mendes"));
        when(contextBuilder.insightFacts()).thenReturn(List.of(
                "Tarefas nos próximos 7 dias: 2 — Atualizar portfólio no Instagram (prazo 23/11/2026), Produção (Carla Mendes, prazo 24/11/2026)."));
        when(ai.generate(anyString(), anyString())).thenReturn("""
                📸 Mariana tem tarefa para atualizar o portfólio no Instagram em 23/11/2026: mantenha as redes em dia.
                📅 O portfólio (23/11/2026) e a produção da Carla Mendes (24/11/2026) vencem nos próximos 7 dias.""");

        assertThat(facade.insights())
                .as("a tarefa do Instagram é avulsa: atribuí-la à Mariana é invenção")
                .containsExactly("📅 O portfólio (23/11/2026) e a produção da Carla Mendes (24/11/2026) vencem nos próximos 7 dias.");
    }

    @Test
    void insightsShouldBeWrittenFromTheFactsNotFromTheWholeContext() {
        when(contextBuilder.insightFacts()).thenReturn(List.of("Eventos futuros: 4."));
        when(contextBuilder.todayLabel()).thenReturn("sábado, 21/11/2026");
        when(ai.generate(anyString(), anyString())).thenReturn("1. 📅 Há 4 eventos futuros na agenda.");

        assertThat(facade.insights()).containsExactly("📅 Há 4 eventos futuros na agenda.");

        ArgumentCaptor<String> system = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(ai).generate(system.capture(), prompt.capture());
        assertThat(system.getValue()).contains("Hoje é sábado, 21/11/2026").doesNotContain("=== DADOS ATUAIS DO SISTEMA ===");
        assertThat(prompt.getValue()).contains("1. Eventos futuros: 4.");
        verify(contextBuilder, never()).build();
    }

    @Test
    void systemPromptShouldUseTheConfiguredBusinessProfile() {
        when(contextBuilder.insightFacts()).thenReturn(List.of("Tarefas para hoje: 2."));
        when(ai.generate(anyString(), anyString())).thenReturn("1. 📅 Duas tarefas vencem hoje: confira a agenda.");

        facade.insights();

        ArgumentCaptor<String> systemPrompt = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userPrompt = ArgumentCaptor.forClass(String.class);
        verify(ai).generate(systemPrompt.capture(), userPrompt.capture());
        assertThat(systemPrompt.getValue()).contains("sistema de gestão de Ateliê Exemplo", "Você ajuda Ana");
        assertThat(userPrompt.getValue()).contains("um insight para Ana:");
    }

    @Test
    void deadlineQuestionsShouldBeAnsweredFromTheFocusedGroupOnly() {
        when(contextBuilder.taskDeadlineFocus("Quais tarefas estão atrasadas?")).thenReturn(Optional.of("TAREFAS ATRASADAS:\n  - Reunião"));
        when(ai.generate(anyString(), anyString())).thenReturn("Reunião");

        facade.chat("Quais tarefas estão atrasadas?");

        ArgumentCaptor<String> system = ArgumentCaptor.forClass(String.class);
        verify(ai).generate(system.capture(), eq("Quais tarefas estão atrasadas?"));
        assertThat(system.getValue()).contains("TAREFAS ATRASADAS:").doesNotContain("=== DADOS ATUAIS DO SISTEMA ===");
        verify(contextBuilder, never()).build();
    }

    @Test
    void focusedPromptShouldOnlyMentionAnEmptyListWhenTheListIsEmpty() {
        when(ai.generate(anyString(), anyString())).thenReturn("ok");
        when(contextBuilder.taskDeadlineFocus("Com itens?")).thenReturn(Optional.of("TAREFAS:\n  - Produção — prazo 24/11/2026"));
        when(contextBuilder.taskDeadlineFocus("Vazia?")).thenReturn(Optional.of("TAREFAS:\n  nenhuma\n"));

        facade.chat("Com itens?");
        facade.chat("Vazia?");

        ArgumentCaptor<String> system = ArgumentCaptor.forClass(String.class);
        verify(ai, times(2)).generate(system.capture(), anyString());
        assertThat(system.getAllValues().get(0))
                .as("com a palavra \"nenhuma\" na instrução, o modelo respondia \"nenhuma\" mesmo com tarefas na lista")
                .contains("listando exatamente as tarefas acima").doesNotContain("Não há tarefas");
        assertThat(system.getAllValues().get(1)).contains("Não há tarefas nesse período");
    }

    @Test
    void shouldReportWhetherTheAssistantIsEnabled() {
        when(ai.isEnabled()).thenReturn(false);
        assertThat(facade.isEnabled()).isFalse();

        when(ai.isEnabled()).thenReturn(true);
        assertThat(facade.isEnabled()).isTrue();
    }
}
