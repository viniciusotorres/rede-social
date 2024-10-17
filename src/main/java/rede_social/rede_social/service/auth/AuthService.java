package rede_social.rede_social.service.auth;

import org.springframework.transaction.annotation.Transactional;
import rede_social.rede_social.dto.auth.ResponseAuthDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rede_social.rede_social.dto.auth.UserAuthDTO;
import rede_social.rede_social.dto.auth.UserRegisterDTO;
import rede_social.rede_social.model.User;
import rede_social.rede_social.repository.UserRepository;

import java.util.logging.Logger;

@Service
public class AuthService {
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    public AuthService(PasswordEncoder passwordEncoder, TokenService tokenService, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public ResponseEntity<ResponseAuthDTO> login(UserAuthDTO userAuth) {
        var user = userRepository.findByEmail(userAuth.email())
                .orElseThrow(() -> {
                    logger.info("Tentativa de login para o usuário: " + userAuth.email());
                    return new RuntimeException("Usuário não encontrado");
                });

        if (user.isPasswordValid(userAuth.password(), passwordEncoder)) {
            var token = tokenService.generateToken(user);
            logger.info("Login efetuado com sucesso para o usuário: " + user.getName());
            return ResponseEntity.ok(new ResponseAuthDTO(token, user.getEmail(), "Usuário logado com sucesso"));
        }

        logger.info("Credenciais inválidas para o usuário: " + user.getName());
        return ResponseEntity.badRequest().body(new ResponseAuthDTO("", "", "Credenciais inválidas"));
    }

    @Transactional
    public ResponseEntity<ResponseAuthDTO> register(UserRegisterDTO userRegister) {
        if (userRepository.findByEmail(userRegister.email()).isPresent()) {
            logger.info("Usuário já registrado: " + userRegister.email());
            return ResponseEntity.badRequest().body(new ResponseAuthDTO("", userRegister.email(), "Usuário já registrado"));
        }
        try {
            var user = new User();
            user.updateFromDTO(userRegister, passwordEncoder);
            userRepository.save(user);

            var token = tokenService.generateToken(user);

            logger.info("Usuário registrado com sucesso: " + userRegister.email());
            return ResponseEntity.ok(new ResponseAuthDTO(token, userRegister.email(), "Usuário registrado com sucesso"));
        } catch (Exception e) {
            logger.info("Erro ao registrar usuário: " + userRegister.email());
            return ResponseEntity.badRequest().body(new ResponseAuthDTO("", userRegister.email(), "Erro ao registrar usuário"));
        }
    }

}


