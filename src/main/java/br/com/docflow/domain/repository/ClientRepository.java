package br.com.docflow.domain.repository;

import br.com.docflow.domain.model.Client;
import java.util.Optional;
import java.util.List;

public interface ClientRepository {
    Client save(Client client);
    Optional<Client> findById(Long id);
    List<Client> findAll();
    void deleteById(Long id);
}
