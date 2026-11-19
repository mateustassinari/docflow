package br.com.docflow.domain.repository;

import br.com.docflow.domain.model.TemplateContrato;

import java.util.List;
import java.util.Optional;

public interface TemplateContratoRepository {
    Optional<TemplateContrato> findById(Long id);
    List<TemplateContrato> findAll();
    TemplateContrato save(TemplateContrato templateContrato);
}
