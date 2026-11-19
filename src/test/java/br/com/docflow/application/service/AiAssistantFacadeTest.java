package br.com.docflow.application.service;

import br.com.docflow.domain.model.BusinessProfile;
import br.com.docflow.domain.service.AiAssistantService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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
}
