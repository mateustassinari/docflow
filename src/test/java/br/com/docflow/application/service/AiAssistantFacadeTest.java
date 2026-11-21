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
        when(contextBuilder.build()).thenReturn("contexto");
        when(ai.generate(anyString(), anyString())).thenReturn("""
                !;
                ;
                Ana tem 6 eventos em atraso e deve priorizar pagá-los.
                !;
                - 💰 Três clientes ainda não pagaram o sinal.""");

        List<String> insights = facade.insights();

        assertThat(insights)
                .as("linhas sem frase (ex.: \"!;\") não podem aparecer como insight")
                .containsExactly(
                        "Ana tem 6 eventos em atraso e deve priorizar pagá-los.",
                        "💰 Três clientes ainda não pagaram o sinal.");
        verify(ai, times(1)).generate(anyString(), anyString());
    }

    @Test
    void shouldRetryOnceWhenEveryLineIsGarbage() {
        when(contextBuilder.build()).thenReturn("contexto");
        when(ai.generate(anyString(), anyString()))
                .thenReturn("!;\n;\n!;")
                .thenReturn("📅 Duas tarefas vencem hoje: confira a agenda.");

        assertThat(facade.insights()).containsExactly("📅 Duas tarefas vencem hoje: confira a agenda.");
        verify(ai, times(2)).generate(anyString(), anyString());
        verify(contextBuilder, times(1)).build();
    }

    @Test
    void shouldReturnEmptyWhenRetryIsAlsoGarbage() {
        when(contextBuilder.build()).thenReturn("contexto");
        when(ai.generate(anyString(), anyString())).thenReturn("!;\n;");

        assertThat(facade.insights())
                .as("sem insight válido, a tela mostra \"Nenhum insight gerado\" em vez de lixo")
                .isEmpty();
        verify(ai, times(2)).generate(anyString(), anyString());
    }

    @Test
    void systemPromptShouldUseTheConfiguredBusinessProfile() {
        when(contextBuilder.build()).thenReturn("contexto");
        when(ai.generate(anyString(), anyString())).thenReturn("📅 Duas tarefas vencem hoje: confira a agenda.");

        facade.insights();

        org.mockito.ArgumentCaptor<String> systemPrompt = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<String> userPrompt = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(ai).generate(systemPrompt.capture(), userPrompt.capture());
        assertThat(systemPrompt.getValue()).contains("sistema de gestão de Ateliê Exemplo", "Você ajuda Ana");
        assertThat(userPrompt.getValue()).contains("para Ana sobre o negócio");
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
