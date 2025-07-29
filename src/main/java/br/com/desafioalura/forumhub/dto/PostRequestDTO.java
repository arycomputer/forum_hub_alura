package br.com.desafioalura.forumhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostRequestDTO(
        @NotBlank(message = "O título não pode estar em branco.")
        @Size(min = 5, max = 100, message = "O título deve ter entre 5 e 100 caracteres.")
        String title,

        @NotBlank(message = "O conteúdo não pode estar em branco.")
        @Size(min = 10, max = 2000, message = "O conteúdo deve ter entre 10 e 2000 caracteres.")
        String content,

        @NotNull(message = "O ID do curso é obrigatório.")
        Long courseId
) {}