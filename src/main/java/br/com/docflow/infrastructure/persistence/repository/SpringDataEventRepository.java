package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.infrastructure.persistence.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataEventRepository extends JpaRepository<EventEntity, Long> {
}
