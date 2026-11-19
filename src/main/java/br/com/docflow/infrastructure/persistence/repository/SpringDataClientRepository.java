package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.infrastructure.persistence.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataClientRepository extends JpaRepository<ClientEntity, Long> {
}
