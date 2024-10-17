package rede_social.rede_social.dto.auth;

public record ResponseAuthDTO(String token, String email, String message) {

    public String getMessage() {
        return message;
    }
}
