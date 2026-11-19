package br.com.docflow.domain.model;

/**
 * Identidade do negócio que usa o sistema: aparece no PDF do contrato, nos e-mails e no
 * assistente de IA. Vem da configuração, para o código não carregar dados de uma pessoa real.
 *
 * @param businessName   nome do negócio (ex.: "Ateliê Exemplo")
 * @param ownerName      nome de quem recebe os avisos; vazio usa uma saudação neutra
 * @param signatureImage caminho de uma imagem de assinatura para o PDF; vazio deixa o espaço em branco
 */
public record BusinessProfile(String businessName, String ownerName, String signatureImage) {

    public BusinessProfile {
        businessName = businessName == null || businessName.isBlank() ? "DocFlow" : businessName.trim();
        ownerName = ownerName == null ? "" : ownerName.trim();
        signatureImage = signatureImage == null ? "" : signatureImage.trim();
    }

    public String greeting() {
        return ownerName.isEmpty() ? "Olá!" : "Olá " + ownerName + "!";
    }

    /** Quem a IA ajuda: o nome da pessoa, ou o próprio negócio quando não há nome configurado. */
    public String addressee() {
        return ownerName.isEmpty() ? "o negócio " + businessName : ownerName;
    }

    public boolean hasSignatureImage() {
        return !signatureImage.isEmpty();
    }
}
