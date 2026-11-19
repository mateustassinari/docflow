package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.repository.ContractRepository;
import br.com.docflow.infrastructure.persistence.mapper.ContractMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("null")
@Repository
@RequiredArgsConstructor
public class ContractRepositoryImpl implements ContractRepository {

    private final SpringDataContractRepository jpaRepository;

    @Override
    public Contract save(Contract contract) {
        var saved = jpaRepository.save(ContractMapper.toEntity(contract));
        contract.setId(saved.getId());
        return contract;
    }

    @Override
    public Optional<Contract> findById(Long id) {
        return jpaRepository.findById(id)
            .map(ContractMapper::toDomain);
    }
    
    @Override
    public List<Contract> findAll() {
        return jpaRepository.findAll().stream()
                .map(ContractMapper::toDomain)
                .toList();
    }

    @Override
    public List<Contract> findByStatus(br.com.docflow.domain.model.ContractStatus status) {
        return jpaRepository.findByStatus(status.name()).stream()
                .map(ContractMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Contract> findByEventId(Long eventId) {
        return jpaRepository.findFirstByEventId(eventId)
                .map(ContractMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
