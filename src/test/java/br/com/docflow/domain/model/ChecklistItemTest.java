package br.com.docflow.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChecklistItemTest {

    @Test
    void shouldRecognizeMolduraCaixaRegardlessOfCaseAccentsAndSpacing() {
        assertThat(item("Moldura e Caixa?").isMolduraCaixa())
                .as("o item criado pela automação precisa ser reconhecido, senão as tarefas condicionais nunca são criadas")
                .isTrue();
        assertThat(item("moldura  e   caixa").isMolduraCaixa()).isTrue();
        assertThat(item("  MOLDURA E CAIXA  ").isMolduraCaixa()).isTrue();
        assertThat(item("Móldura e Caíxa").isMolduraCaixa()).isTrue();
        assertThat(item("Moldura").isMolduraCaixa()).isFalse();
        assertThat(item(null).isMolduraCaixa()).isFalse();
    }

    private ChecklistItem item(String texto) {
        return new ChecklistItem(null, texto, false, null);
    }
}
