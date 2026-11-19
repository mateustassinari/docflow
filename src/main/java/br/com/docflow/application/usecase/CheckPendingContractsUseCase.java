package br.com.docflow.application.usecase;

import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.ContractStatus;
import br.com.docflow.domain.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckPendingContractsUseCase {

    private final ContractRepository contractRepository;
    private final SynchronizeContractSignatureUseCase synchronizeContractSignatureUseCase;

    public void execute() {
        List<Contract> pendingContracts = contractRepository.findByStatus(ContractStatus.ENVIADO);

        for (Contract contract : pendingContracts) {
            if (contract.getAutentiqueId() == null || contract.getAutentiqueId().isBlank()) {
                continue;
            }

            try {
                synchronizeContractSignatureUseCase.execute(contract);
            } catch (Exception e) {
                System.err.println("Falha ao checar status do contrato " + contract.getId() + ": " + e.getMessage());
            }
        }
    }
}
