package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.domain.model.TemplateContrato;
import br.com.docflow.domain.repository.TemplateContratoRepository;
import br.com.docflow.infrastructure.persistence.mapper.TemplateContratoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("null")
@Repository
@RequiredArgsConstructor
public class TemplateContratoRepositoryImpl implements TemplateContratoRepository {

    private final SpringDataTemplateContratoRepository jpaRepository;

    @Override
    public Optional<TemplateContrato> findById(Long id) {
        return jpaRepository.findById(id).map(TemplateContratoMapper::toDomain);
    }

    @Override
    public List<TemplateContrato> findAll() {
        return jpaRepository.findAll().stream()
                .map(TemplateContratoMapper::toDomain)
                .toList();
    }

    @Override
    public TemplateContrato save(TemplateContrato templateContrato) {
        return TemplateContratoMapper.toDomain(
                jpaRepository.save(TemplateContratoMapper.toEntity(templateContrato))
        );
    }
}
