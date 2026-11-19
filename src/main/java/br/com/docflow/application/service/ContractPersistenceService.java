package br.com.docflow.application.service;

import br.com.docflow.domain.exception.BusinessException;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ContractPersistenceService {

    private final ContractRepository contractRepository;

    @Transactional
    public Contract saveNewContract(Contract contract) {
        ensureEventHasNoOtherContract(contract);
        return contractRepository.save(contract);
    }

    @Transactional
    public Contract markAsSent(Contract contract, String autentiqueId) {
        contract.markAsSent(autentiqueId);
        return contractRepository.save(contract);
    }

    @Transactional
    public Contract markAsSigned(Contract contract, String signedPdfUrl) {
        contract.attachPdf(signedPdfUrl);
        contract.markAsSigned();
        return contractRepository.save(contract);
    }

    @Transactional
    public Contract markAsDraftForReview(Contract contract) {
        contract.markAsDraftForReview();
        return contractRepository.save(contract);
    }

    private void ensureEventHasNoOtherContract(Contract contract) {
        if (contract == null || contract.getEvent() == null || contract.getEvent().getId() == null) {
            throw new BusinessException("EVENT_REQUIRED", "Selecione um evento para vincular o contrato");
        }

        Long eventId = contract.getEvent().getId();
        contractRepository.findByEventId(eventId)
                .filter(existing -> !Objects.equals(existing.getId(), contract.getId()))
                .ifPresent(existing -> {
                    throw new BusinessException("CONTRACT_ALREADY_EXISTS", "Este evento ja possui contrato gerado");
                });
    }
}
