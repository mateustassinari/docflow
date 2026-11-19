package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.domain.model.ServiceTypeCostDefault;
import br.com.docflow.domain.repository.ServiceTypeCostDefaultRepository;
import br.com.docflow.infrastructure.persistence.mapper.ServiceTypeCostDefaultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("null")
@Repository
@RequiredArgsConstructor
public class ServiceTypeCostDefaultRepositoryImpl implements ServiceTypeCostDefaultRepository {

    private final SpringDataServiceTypeCostDefaultRepository jpaRepository;

    @Override
    public List<ServiceTypeCostDefault> findAll() {
        return jpaRepository.findAll().stream()
                .map(ServiceTypeCostDefaultMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ServiceTypeCostDefault> findByTipoServico(String tipoServico) {
        return jpaRepository.findById(tipoServico)
                .map(ServiceTypeCostDefaultMapper::toDomain);
    }

    @Override
    public ServiceTypeCostDefault save(ServiceTypeCostDefault costDefault) {
        return ServiceTypeCostDefaultMapper.toDomain(
                jpaRepository.save(ServiceTypeCostDefaultMapper.toEntity(costDefault))
        );
    }
}
