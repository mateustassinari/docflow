package br.com.docflow.application.service;

import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotifyContractSigned {

    private final NotificationService notificationService;

    public void execute(Contract contract) {
        notificationService.sendContractSignedAlert(contract);
    }
}
