package br.com.docflow.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessProfileTest {

    @Test
    void shouldGreetTheOwnerByNameWhenConfigured() {
        BusinessProfile profile = new BusinessProfile("Ateliê Exemplo", " Ana ", "");

        assertThat(profile.greeting()).isEqualTo("Olá Ana!");
        assertThat(profile.addressee()).isEqualTo("Ana");
        assertThat(profile.hasSignatureImage()).isFalse();
    }

    @Test
    void shouldFallBackToNeutralValuesWhenNothingIsConfigured() {
        BusinessProfile profile = new BusinessProfile(null, null, null);

        assertThat(profile.businessName()).isEqualTo("DocFlow");
        assertThat(profile.greeting()).as("sem nome configurado, a saudação é neutra").isEqualTo("Olá!");
        assertThat(profile.addressee()).isEqualTo("o negócio DocFlow");
    }
}
