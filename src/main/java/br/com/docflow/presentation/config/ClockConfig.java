package br.com.docflow.presentation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Relógio único da aplicação. Injetar {@link Clock} em vez de chamar {@code LocalDate.now()}
 * permite testar regras de data com um relógio fixo.
 */
@Configuration
public class ClockConfig {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    @Bean
    public Clock clock() {
        return Clock.system(SAO_PAULO);
    }
}
