package br.com.desafioalura.forumhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseDTO(
        Long id,
        @NotBlank(message = "O título não pode estar vazio")
        @Size(min = 5, max = 100, message = "O título deve ter entre 5 e 100 caracteres")
        String name
) {}
