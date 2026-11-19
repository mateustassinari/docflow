package br.com.docflow.application.usecase;

import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListContractsUseCase {

    private final ContractRepository contractRepository;

    public List<Contract> execute() {
        return contractRepository.findAll();
    }
}
