package br.com.docflow.infrastructure.cron;

import br.com.docflow.application.usecase.CheckPendingContractsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutentiqueStatusPoller {

    private final CheckPendingContractsUseCase checkPendingContractsUseCase;

    // Roda a cada 30 minutos (1800000 ms)
    @Scheduled(fixedDelay = 1800000)
    public void pollAutentiqueStatus() {
        checkPendingContractsUseCase.execute();
    }
}
