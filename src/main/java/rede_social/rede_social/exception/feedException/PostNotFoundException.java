package rede_social.rede_social.exception.feedException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando um post não é encontrado.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Post não encontrado")
public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Long postId) {
        super("Post com ID " + postId + " não encontrado.");
    }
}
