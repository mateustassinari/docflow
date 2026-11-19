package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.infrastructure.persistence.entity.ServiceTypeCostDefaultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataServiceTypeCostDefaultRepository
        extends JpaRepository<ServiceTypeCostDefaultEntity, String> {
}
