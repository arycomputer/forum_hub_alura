package br.com.desafioalura.forumhub.dto;

import br.com.desafioalura.forumhub.model.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDTO {
    private String email;
    private String password;
    @Builder.Default
    private boolean active = true;
    private User.Role role;
}
