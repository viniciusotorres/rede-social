package rede_social.rede_social.exception.authException;

public class UserNotVerifiedException extends RuntimeException {
    public UserNotVerifiedException(String name) {
        super("Usuário não verificado: " + name);
    }
}