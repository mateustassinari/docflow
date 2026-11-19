package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.domain.model.Client;
import br.com.docflow.domain.repository.ClientRepository;
import br.com.docflow.infrastructure.persistence.mapper.ClientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("null")
@Repository
@RequiredArgsConstructor
public class ClientRepositoryImpl implements ClientRepository {

    private final SpringDataClientRepository jpaRepository;

    @Override
    public Client save(Client client) {
        return ClientMapper.toDomain(
            jpaRepository.save(ClientMapper.toEntity(client))
        );
    }

    @Override
    public Optional<Client> findById(Long id) {
        return jpaRepository.findById(id).map(ClientMapper::toDomain);
    }

    @Override
    public List<Client> findAll() {
        return jpaRepository.findAll().stream()
                .map(ClientMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
