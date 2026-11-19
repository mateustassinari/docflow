package br.com.docflow.domain.repository;

import br.com.docflow.domain.model.Contract;
import java.util.Optional;
import java.util.List;

public interface ContractRepository {
    Contract save(Contract contract);
    Optional<Contract> findById(Long id);
    List<Contract> findAll();
    List<Contract> findByStatus(br.com.docflow.domain.model.ContractStatus status);
    Optional<Contract> findByEventId(Long eventId);
    void deleteById(Long id);
}
