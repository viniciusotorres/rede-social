package rede_social.rede_social.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rede_social.rede_social.model.User;
import rede_social.rede_social.repository.UserRepository;

@Service
public class TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${api.security.token.secret}")
    public String secret;

    @Autowired
    private UserRepository userRepository;

    public String generateToken(User user) {
        if (secret == null) {
            throw new RuntimeException("Secret key is not set");
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("auth-login")
                    .withClaim("id", user.getId())
                    .withClaim("email", user.getEmail())
                    .sign(algorithm);
            logger.info("Token generated for user: {}", user.getEmail());
            return token;
        } catch (JWTCreationException exception) {
            logger.error("Error generating token for user: {}", user.getEmail(), exception);
            throw new RuntimeException("Error authenticating user", exception);
        }
    }

    public String validateToken(String token) {
        if (secret == null) {
            throw new RuntimeException("Secret key is not set");
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String email = JWT.require(algorithm)
                    .withIssuer("auth-login")
                    .build()
                    .verify(token)
                    .getClaim("email").asString();
            logger.info("Token successfully validated for user: {}", email);
            return email;
        } catch (JWTVerificationException exception) {
            logger.error("Error validating token: {}", exception.getMessage());
            return null;
        }
    }

    public User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

}
