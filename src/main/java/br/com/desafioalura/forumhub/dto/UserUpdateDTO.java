package br.com.desafioalura.forumhub.dto;

import br.com.desafioalura.forumhub.model.User;

public record UserUpdateDTO(
        Long id,
        String email,
        User.Role role,
        boolean active
) {
}
