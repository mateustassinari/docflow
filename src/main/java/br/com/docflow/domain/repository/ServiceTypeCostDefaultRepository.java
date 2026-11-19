package br.com.docflow.domain.repository;

import br.com.docflow.domain.model.ServiceTypeCostDefault;
import java.util.List;
import java.util.Optional;

public interface ServiceTypeCostDefaultRepository {
    List<ServiceTypeCostDefault> findAll();
    Optional<ServiceTypeCostDefault> findByTipoServico(String tipoServico);
    ServiceTypeCostDefault save(ServiceTypeCostDefault costDefault);
}
