package br.com.docflow.application.usecase;

import br.com.docflow.domain.model.ServiceTypeCostDefault;
import br.com.docflow.domain.repository.ServiceTypeCostDefaultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetServiceTypeCostDefaultsUseCase {

    private final ServiceTypeCostDefaultRepository repository;

    public List<ServiceTypeCostDefault> execute() {
        return repository.findAll();
    }
}
