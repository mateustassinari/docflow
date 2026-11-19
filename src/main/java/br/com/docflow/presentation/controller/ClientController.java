package br.com.docflow.presentation.controller;

import br.com.docflow.presentation.dto.ClientResponse;
import br.com.docflow.domain.model.Client;
import br.com.docflow.domain.repository.ClientRepository;
import br.com.docflow.presentation.dto.ClientRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientRepository clientRepository;

    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        Client client = new Client();
        client.setNome(request.getNome());
        client.setCpfCnpj(request.getCpfCnpj());
        client.setEmail(request.getEmail());
        client.setTelefone(request.getTelefone());

        return ResponseEntity.ok(ClientResponse.from(clientRepository.save(client)));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> listAll() {
        return ResponseEntity.ok(clientRepository.findAll().stream().map(ClientResponse::from).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return clientRepository.findById(id).map(existing -> {
            existing.setNome(request.getNome());
            existing.setCpfCnpj(request.getCpfCnpj());
            existing.setEmail(request.getEmail());
            existing.setTelefone(request.getTelefone());
            return ResponseEntity.ok(ClientResponse.from(clientRepository.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (clientRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        clientRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
