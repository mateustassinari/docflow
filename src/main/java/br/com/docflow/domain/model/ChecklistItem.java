package br.com.docflow.domain.model;

import java.text.Normalizer;
import java.util.Locale;

public record ChecklistItem(Long id, String texto, boolean concluido, Integer ordem) {

    private static final String MOLDURA_E_CAIXA = "moldura e caixa";

    /**
     * Reconhece o item "Moldura e Caixa?" do checklist de compra de material da pintura,
     * ignorando caixa, acentos, pontuação e espaços repetidos.
     */
    public boolean isMolduraCaixa() {
        return normalize(texto).replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ").trim()
                .equals(MOLDURA_E_CAIXA);
    }

    private static String normalize(String value) {
        String text = value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
