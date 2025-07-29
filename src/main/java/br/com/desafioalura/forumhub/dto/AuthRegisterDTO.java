package br.com.desafioalura.forumhub.dto;

import br.com.desafioalura.forumhub.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterDTO(

        @NotBlank(message = "O email não pode estar vazio.")
        @Email(message = "Formato de email inválido.")
        @Size(min = 5, max = 100, message = "O email deve ter entre 5 e 100 caracteres.")
        String email,

        @NotBlank(message = "A senha não pode estar vazia.")
        @Size(min = 8, max = 12, message = "A senha deve ter entre 8 e 12 caracteres.")
        @ValidPassword // Aplica a validação de senha personalizada
        String password
){}
