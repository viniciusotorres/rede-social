package rede_social.rede_social.exception.authException;

public class ConfirmationCodeNotFoundException extends RuntimeException {
    public ConfirmationCodeNotFoundException(String email) {
        super("Código de confirmação não encontrado para: " + email);
    }
}
