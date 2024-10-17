package rede_social.rede_social.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rede_social.rede_social.model.User;
import rede_social.rede_social.repository.UserRepository;
import rede_social.rede_social.service.auth.TokenService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TokenServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        tokenService.secret = "testSecret";
    }

    @Test
    public void testGenerateTokenSuccess() {
        User user = new User();
        user.setId(Long.valueOf(1L));
        user.setEmail("test@example.com");

        String token = tokenService.generateToken(user);

        assertNotNull(token);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    public void testGenerateTokenFailure() {
        User user = new User();
        user.setId(Long.valueOf(1L));
        user.setEmail("test@example.com");

        tokenService.secret = null; // Simulate a failure by setting secret to null

        assertThrows(RuntimeException.class, () -> {
            tokenService.generateToken(user);
        });
    }

    @Test
    public void testValidateTokenSuccess() {
        User user = new User();
        user.setId(Long.valueOf(1L));
        user.setEmail("test@example.com");

        String token = tokenService.generateToken(user);

        String subject = tokenService.validateToken(token);

        assertNotNull(subject);
        assertEquals("test@example.com", subject);
    }

    @Test
    public void testValidateTokenFailure() {
        String invalidToken = "invalidToken";

        String subject = tokenService.validateToken(invalidToken);

        assertNull(subject);
    }

    @Test
    public void testGetUserSuccess() {
        User user = new User();
        user.setId(Long.valueOf(1L));
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = tokenService.getUser("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    public void testGetUserFailure() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            tokenService.getUser("test@example.com");
        });
    }

    @Test
    public void testGenerateTokenExceptionHandling() {
        User user = new User();
        user.setId(Long.valueOf(1L));
        user.setEmail("test@example.com");

        Algorithm algorithm = Algorithm.HMAC256("testSecret");
        JWTCreator.Builder builder = mock(JWTCreator.Builder.class);
        when(builder.withIssuer(anyString())).thenReturn(builder);
        when(builder.withClaim(anyString(), anyString())).thenReturn(builder);
        when(builder.sign(algorithm)).thenThrow(new JWTCreationException("Error", null));

        try (var jwtMock = mockStatic(JWT.class)) {
            jwtMock.when(JWT::create).thenReturn(builder);

            assertThrows(RuntimeException.class, () -> {
                tokenService.generateToken(user);
            });
        }
    }

}