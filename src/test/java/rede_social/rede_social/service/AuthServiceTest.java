package rede_social.rede_social.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import rede_social.rede_social.dto.auth.UserRegisterDTO;
import rede_social.rede_social.model.User;
import rede_social.rede_social.repository.UserRepository;
import rede_social.rede_social.service.auth.AuthService;
import rede_social.rede_social.service.auth.TokenService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    public AuthServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUserAlreadyExists() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test@example.com", "password", "Test User", "1990-01-01", "http://example.com/photo.jpg", "Bio", "2024-10-16T20:00:00", "2024-10-16T20:00:00");
        when(userRepository.findByEmail(userRegisterDTO.email())).thenReturn(Optional.of(new User()));

        var response = authService.register(userRegisterDTO);

        assertEquals("Usuário já registrado", response.getBody().getMessage());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    public void testRegisterSuccess() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test@example.com", "password", "Test User", "1990-01-01", "http://example.com/photo.jpg", "Bio", "2024-10-16T20:00:00", "2024-10-16T20:00:00");
        when(userRepository.findByEmail(userRegisterDTO.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userRegisterDTO.password())).thenReturn("encodedPassword");
        when(tokenService.generateToken(any(User.class))).thenReturn("token");

        var response = authService.register(userRegisterDTO);

        assertEquals("Usuário registrado com sucesso", response.getBody().getMessage());
        verify(userRepository, times(1)).save(any(User.class));
    }
}
