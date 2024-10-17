package rede_social.rede_social.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterDTO(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is mandatory")
        String email,
        @NotBlank(message = "Password is mandatory")
        @Size(min = 6, message = "Password should have at least 6 characters")
        String password,
        @NotBlank(message = "Name is mandatory")
        String name,
        @NotBlank(message = "Birthdate is mandatory")
        String birthdate,
        @NotBlank(message = "Photo is mandatory")
        String photo,
        String bio,
        String createdAt,
        String updatedAt) {
}