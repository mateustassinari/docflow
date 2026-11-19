package br.com.docflow.domain.model;

import lombok.Data;

@Data
public class Client {
    private Long id;
    private String nome;
    private String cpfCnpj;
    private String email;
    private String telefone;
}
