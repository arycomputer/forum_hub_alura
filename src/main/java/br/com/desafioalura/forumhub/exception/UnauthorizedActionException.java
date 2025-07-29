package br.com.desafioalura.forumhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }

    public UnauthorizedActionException(String action, String userEmail, String resourceType, Long resourceId) {
        super(String.format("Usuário '%s' não tem permissão para %s este(a) %s com ID '%d'.", userEmail, action, resourceType, resourceId));
    }
}
