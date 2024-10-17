package rede_social.rede_social.service.auth;

import org.springframework.transaction.annotation.Transactional;
import rede_social.rede_social.dto.auth.ResponseAuthDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rede_social.rede_social.dto.auth.UserAuthDTO;
import rede_social.rede_social.dto.auth.UserRegisterDTO;
import rede_social.rede_social.model.ConfirmationCode;
import rede_social.rede_social.model.User;
import rede_social.rede_social.repository.ConfirmationCodeRepository;
import rede_social.rede_social.repository.UserRepository;
import rede_social.rede_social.service.email.EmailService;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Service
public class AuthService {
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    private final EmailService emailService;
    private final ConfirmationCodeRepository confirmationCodeRepository;

    public AuthService(PasswordEncoder passwordEncoder, TokenService tokenService, UserRepository userRepository, EmailService emailService, ConfirmationCodeRepository confirmationCodeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.confirmationCodeRepository = confirmationCodeRepository;
    }

    @Transactional
    public ResponseEntity<ResponseAuthDTO> login(UserAuthDTO userAuth) {
        var user = userRepository.findByEmail(userAuth.email())
                .orElseThrow(() -> {
                    logger.info("Tentativa de login para o usuário: " + userAuth.email());
                    return new RuntimeException("Usuário não encontrado");
                });

        if (!user.isVerified()){
            logger.info("Usuário não verificado: " + user.getName());
            return ResponseEntity.badRequest().body(new ResponseAuthDTO("", user.getEmail(), "Usuário não verificado"));
        }

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

            String code = CodeGenerator.generateCode();
            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(10);

            ConfirmationCode confirmationCode = new ConfirmationCode(userRegister.email(), code, expirationTime);
            confirmationCodeRepository.save(confirmationCode);

            emailService.sendConfirmationEmail(userRegister.email(),
                    "Código de Confirmação", "Seu código de confirmação é: " + code);

            var token = tokenService.generateToken(user);

            logger.info("Usuário registrado com sucesso: " + userRegister.email());
            return ResponseEntity.ok(new ResponseAuthDTO(token, userRegister.email(), "Usuário registrado com sucesso"));
        } catch (Exception e) {
            logger.info("Erro ao registrar usuário: " + userRegister.email());
            return ResponseEntity.badRequest().body(new ResponseAuthDTO("", userRegister.email(), "Erro ao registrar usuário"));
        }
    }

    @Transactional
    public ResponseEntity<String> verifyCode(String email, String code) {
        var confirmationCode = confirmationCodeRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("Código de confirmação não encontrado"));

        if (confirmationCode.getCode().equals(code) && confirmationCode.getExpirationDate().isAfter(LocalDateTime.now())) {
            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            user.setVerified(true);
            userRepository.save(user);
            return ResponseEntity.ok("Código de confirmação válido");
        } else {
            return ResponseEntity.badRequest().body("Código de confirmação inválido ou expirado");
        }
    }

}


