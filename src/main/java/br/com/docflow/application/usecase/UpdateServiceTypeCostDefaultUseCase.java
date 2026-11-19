package br.com.docflow.application.usecase;

import br.com.docflow.domain.exception.NotFoundException;
import br.com.docflow.domain.model.ServiceTypeCostDefault;
import br.com.docflow.domain.repository.ServiceTypeCostDefaultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UpdateServiceTypeCostDefaultUseCase {

    private final ServiceTypeCostDefaultRepository repository;

    public ServiceTypeCostDefault execute(String tipoServico, BigDecimal custoDefault) {
        ServiceTypeCostDefault costDefault = repository.findByTipoServico(tipoServico)
                .orElseThrow(() -> new NotFoundException("COST_DEFAULT_NOT_FOUND", "Tipo de serviço não encontrado"));

        costDefault.setCustoDefault(custoDefault);
        return repository.save(costDefault);
    }
}
