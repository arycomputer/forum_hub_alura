package br.com.desafioalura.forumhub.dto;

import br.com.desafioalura.forumhub.model.User;

public record UserResponseDTO(
        Long id,
        String email,
        User.Role role
) {

    public static UserResponseDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }
}
