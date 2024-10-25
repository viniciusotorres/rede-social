package rede_social.rede_social.exception.authException;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("Usuário não encontrado: " + email);
    }
}