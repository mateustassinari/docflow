package br.com.docflow.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRequest {

    @NotBlank(message = "O nome do cliente é obrigatório")
    private String nome;

    @NotBlank(message = "O CPF/CNPJ é obrigatório")
    private String cpfCnpj;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O formato do e-mail é inválido")
    private String email;

    @NotBlank(message = "O telefone é obrigatório")
    private String telefone;
}
