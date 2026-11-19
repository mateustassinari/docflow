package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.infrastructure.persistence.entity.TemplateContratoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTemplateContratoRepository extends JpaRepository<TemplateContratoEntity, Long> {
}
