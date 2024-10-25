package rede_social.rede_social.exception.authException;

public class InvalidConfirmationCodeException extends RuntimeException {
    public InvalidConfirmationCodeException() {
        super("Código de confirmação inválido ou expirado.");
    }

    public InvalidConfirmationCodeException(String email) {
        super("Código de confirmação inválido ou expirado para: " + email);
    }
}
