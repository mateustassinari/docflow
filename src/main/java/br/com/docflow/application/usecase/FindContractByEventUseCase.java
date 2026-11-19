package br.com.docflow.application.usecase;

import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindContractByEventUseCase {

    private final ContractRepository contractRepository;

    public Contract execute(Long eventId) {
        return contractRepository.findByEventId(eventId)
                .orElseThrow(() -> new NotFoundException("CONTRACT_NOT_FOUND", "Contrato nao encontrado para o evento informado"));
    }
}
