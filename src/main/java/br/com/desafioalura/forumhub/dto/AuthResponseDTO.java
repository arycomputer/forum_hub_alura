package br.com.desafioalura.forumhub.dto;

import br.com.desafioalura.forumhub.model.User;

public record AuthResponseDTO(
        String email,
        String token,
        User.Role role
){}

