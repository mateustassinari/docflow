package br.com.docflow.application.service;

import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.ContractStatus;
import br.com.docflow.domain.service.SignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckSignatureStatus {

    private final SignatureService signatureService;

    public boolean shouldSynchronize(Contract contract) {
        return contract != null
                && contract.getStatus() == ContractStatus.ENVIADO
                && contract.getAutentiqueId() != null
                && !contract.getAutentiqueId().isBlank();
    }

    public boolean isSigned(Contract contract) throws Exception {
        return signatureService.isContractSigned(contract.getAutentiqueId());
    }
}
