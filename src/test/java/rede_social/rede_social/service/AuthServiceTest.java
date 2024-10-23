package rede_social.rede_social.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import rede_social.rede_social.dto.auth.UserAuthDTO;
import rede_social.rede_social.dto.auth.UserRegisterDTO;
import rede_social.rede_social.model.ConfirmationCode;
import rede_social.rede_social.model.User;
import rede_social.rede_social.repository.ConfirmationCodeRepository;
import rede_social.rede_social.repository.UserRepository;
import rede_social.rede_social.service.auth.AuthService;
import rede_social.rede_social.service.auth.TokenService;
import rede_social.rede_social.service.email.EmailService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private ConfirmationCodeRepository confirmationCodeRepository;

    @InjectMocks
    private AuthService authService;

    public AuthServiceTest() {
        MockitoAnnotations.openMocks(this);
    }



    @Test
    public void testLoginSuccess() {
        UserAuthDTO userAuthDTO = new UserAuthDTO("test@example.com", "password123");
        User user = new User();
        user.setPassword("encodedPassword");
        user.setVerified(true); // Ensure the user is verified

        when(userRepository.findByEmail(userAuthDTO.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userAuthDTO.password(), "encodedPassword")).thenReturn(true);
        when(tokenService.generateToken(any(User.class))).thenReturn("token");

        var response = authService.login(userAuthDTO);

        assertEquals("Usuário logado com sucesso", response.getBody().getMessage());
        verify(userRepository, times(1)).findByEmail(userAuthDTO.email());
    }

    @Test
    public void testLoginUserNotFound() {
        UserAuthDTO userAuthDTO = new UserAuthDTO("test@exa.com", "password");
        when(userRepository.findByEmail(userAuthDTO.email())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login(userAuthDTO);
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(userAuthDTO.email());
    }
}
