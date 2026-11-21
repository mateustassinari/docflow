package br.com.docflow.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRequest {

    @NotBlank(message = "O nome do cliente é obrigatório")
    @Size(max = 255, message = "O nome pode ter até 255 caracteres")
    private String nome;

    @NotBlank(message = "O CPF/CNPJ é obrigatório")
    @Size(max = 50, message = "O CPF/CNPJ pode ter até 50 caracteres")
    private String cpfCnpj;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O formato do e-mail é inválido")
    @Size(max = 255, message = "O e-mail pode ter até 255 caracteres")
    private String email;

    @NotBlank(message = "O telefone é obrigatório")
    @Size(max = 50, message = "O telefone pode ter até 50 caracteres")
    private String telefone;
}
