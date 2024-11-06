package rede_social.rede_social.exception.feedException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando uma ação inválida é tentada, como curtir ou descurtir mais de uma vez.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Ação inválida")
public class InvalidActionException extends RuntimeException {
    public InvalidActionException(String action) {
        super("Ação inválida: " + action);
    }
}
