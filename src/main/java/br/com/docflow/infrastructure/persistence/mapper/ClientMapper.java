package br.com.docflow.infrastructure.persistence.mapper;

import br.com.docflow.domain.model.Client;
import br.com.docflow.infrastructure.persistence.entity.ClientEntity;

public class ClientMapper {

    public static ClientEntity toEntity(Client client) {
        if (client == null) return null;
        
        ClientEntity entity = new ClientEntity();
        entity.setId(client.getId());
        entity.setNome(client.getNome());
        entity.setCpfCnpj(client.getCpfCnpj());
        entity.setEmail(client.getEmail());
        entity.setTelefone(client.getTelefone());
        
        return entity;
    }

    public static Client toDomain(ClientEntity entity) {
        if (entity == null) return null;
        
        Client client = new Client();
        client.setId(entity.getId());
        client.setNome(entity.getNome());
        client.setCpfCnpj(entity.getCpfCnpj());
        client.setEmail(entity.getEmail());
        client.setTelefone(entity.getTelefone());
        
        return client;
    }
}
