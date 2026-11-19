package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.infrastructure.persistence.entity.ContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataContractRepository extends JpaRepository<ContractEntity, Long> {
    List<ContractEntity> findByStatus(String status);
    java.util.Optional<ContractEntity> findFirstByEventId(Long eventId);
}
