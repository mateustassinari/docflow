package br.com.docflow.application.usecase;

import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindContractByIdUseCase {

    private final ContractRepository contractRepository;

    public Contract execute(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CONTRACT_NOT_FOUND", "Contrato nao encontrado"));
    }
}
