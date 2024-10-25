package rede_social.rede_social.exception.authException;

public class UserAlreadyRegisteredException extends RuntimeException {
    public UserAlreadyRegisteredException(String email) {
        super("Usuário já registrado: " + email);
    }

}
