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
    private String secret;

    @Autowired
    private UserRepository userRepository;

    public String  generateToken(User user) {
       try {
           Algorithm algorithm = Algorithm.HMAC256(secret);
              String token = JWT.create()
                     .withIssuer("auth-login")
                     .withClaim("id", user.getId())
                     .withClaim("email", user.getEmail())
                     .sign(algorithm);
              logger.info("Token gerado para o usuário:  {}", user.getEmail());
           return token;
       } catch (JWTCreationException exception) {
           logger.error("Erro ao gerar token para o usuário: {}", user.getEmail(), exception);
           throw new RuntimeException("Erro ao autenticar usuário", exception);
       }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String subject = JWT.require(algorithm)
                    .withIssuer("auth-login")
                    .build()
                    .verify(token)
                    .getSubject();
            logger.info("Token validado com sucesso para o usuário: {}", subject);
            return subject;
        } catch (JWTVerificationException exception) {
            logger.error("Erro ao validar token: {}", exception.getMessage());
            return null;
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

}
