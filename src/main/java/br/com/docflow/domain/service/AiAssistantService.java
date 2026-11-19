package br.com.docflow.domain.service;

/**
 * Porta de saída para o assistente de IA. Mantém a aplicação agnóstica do provedor
 * (Ollama local, Gemini, etc.) — o adaptador concreto vive em infrastructure/external/ai.
 */
public interface AiAssistantService {

    /**
     * Gera uma resposta em texto a partir de uma instrução de sistema e uma mensagem do usuário.
     */
    String generate(String systemPrompt, String userPrompt);

    /**
     * Indica se o assistente está habilitado por configuração.
     */
    boolean isEnabled();
}
