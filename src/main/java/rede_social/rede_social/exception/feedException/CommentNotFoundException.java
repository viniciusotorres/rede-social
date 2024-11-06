package rede_social.rede_social.exception.feedException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando um comentário não é encontrado.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Comentário não encontrado")
public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(Long commentId) {
        super("Comentário com ID " + commentId + " não encontrado.");
    }
}
