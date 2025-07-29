package br.com.desafioalura.forumhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDTO {
    @NotBlank(message = "O título não pode estar vazio")
    @Size(min = 5, max = 100, message = "O título deve ter entre 5 e 100 caracteres")
    private String content;
}