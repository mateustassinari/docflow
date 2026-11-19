package br.com.docflow.presentation.dto;

import br.com.docflow.domain.model.Client;

/** Cliente na API. Separado do domínio para o JSON não mudar sozinho quando o modelo mudar. */
public record ClientResponse(
        Long id,
        String nome,
        String cpfCnpj,
        String email,
        String telefone
) {

    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getNome(),
                client.getCpfCnpj(),
                client.getEmail(),
                client.getTelefone()
        );
    }
}
