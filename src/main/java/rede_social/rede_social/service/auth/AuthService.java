package rede_social.rede_social.service.auth;

import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import rede_social.rede_social.dto.auth.ResponseAuthDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rede_social.rede_social.dto.auth.UserAuthDTO;
import rede_social.rede_social.dto.auth.UserRegisterDTO;
import rede_social.rede_social.dto.auth.VerifcationDTO;
import rede_social.rede_social.model.ConfirmationCode;
import rede_social.rede_social.model.User;
import rede_social.rede_social.repository.ConfirmationCodeRepository;
import rede_social.rede_social.repository.UserRepository;
import rede_social.rede_social.service.email.EmailService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
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

        if (!user.isVerified()) {
            logger.info("Usuário não verificado: " + user.getName());
            return ResponseEntity.badRequest().body(new ResponseAuthDTO("", user.getEmail(), "Usuário não verificado", null));
        }

        if (user.isPasswordValid(userAuth.password(), passwordEncoder)) {
            var token = tokenService.generateToken(user);
            logger.info("Login efetuado com sucesso para o usuário: " + user.getName());
            return ResponseEntity.ok(new ResponseAuthDTO(token, user.getEmail(), "Usuário logado com sucesso", user.getId()));
        }

        logger.info("Credenciais inválidas para o usuário: " + user.getName());
        return ResponseEntity.badRequest().body(new ResponseAuthDTO("", "", "Credenciais inválidas", null));
    }

    @Transactional
    public ResponseEntity<ResponseAuthDTO> register(UserRegisterDTO userRegister) {
        if (userRepository.findByEmail(userRegister.email()).isPresent()) {
            logger.info("Usuário já registrado: " + userRegister.email());
            return ResponseEntity.badRequest().body(new ResponseAuthDTO("", userRegister.email(), "Usuário já registrado", null));
        }
        try {
            var user = new User();
            user.updateFromDTO(userRegister, passwordEncoder);

            if (!isValidImageType(userRegister.photo())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image type. Only JPG, JPEG, and PNG are allowed.");
            }

            String imageBase64 = saveImage(userRegister.photo());
            user.setPhoto(imageBase64.getBytes());

            user.setVerified(false);
            userRepository.save(user);

            String code = CodeGenerator.generateCode();
            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(10);

            ConfirmationCode confirmationCode = new ConfirmationCode(userRegister.email(), code, expirationTime);
            confirmationCodeRepository.save(confirmationCode);

            sendCode(userRegister.email());

            var token = tokenService.generateToken(user);

            logger.info("Usuário registrado com sucesso: " + userRegister.email());
            return ResponseEntity.ok(new ResponseAuthDTO(token, userRegister.email(), "Usuário registrado com sucesso", user.getId()));
        } catch (Exception e) {
            logger.info("Erro ao registrar usuário: " + userRegister.email());
            return ResponseEntity.badRequest().body(new ResponseAuthDTO("", userRegister.email(), "Erro ao registrar usuário", null));
        }
    }

    private boolean isValidImageType(MultipartFile image) {
        String contentType = image.getContentType();
        return contentType.equals("image/jpeg") || contentType.equals("image/jpg") || contentType.equals("image/png");
    }

    private String saveImage(MultipartFile image) throws IOException {
        byte[] bytes = image.getBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Transactional
    public ResponseEntity<String> verifyCode(VerifcationDTO verifcationDTO) {
        var confirmationCode = confirmationCodeRepository.findById(verifcationDTO.email())
                .orElseThrow(() -> new RuntimeException("Código de confirmação não encontrado"));

        if (confirmationCode.getCode().equals(verifcationDTO.code()) && confirmationCode.getExpirationDate().isAfter(LocalDateTime.now())) {
            var user = userRepository.findByEmail(verifcationDTO.email())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            user.setVerified(true);
            userRepository.save(user);
            return ResponseEntity.ok("Código de confirmação válido");
        } else {
            return ResponseEntity.badRequest().body("Código de confirmação inválido ou expirado");
        }
    }

    public ResponseEntity<String> sendCode(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (user.isVerified()) {
            return ResponseEntity.badRequest().body("Usuário já verificado");
        }

        String code = CodeGenerator.generateCode();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(10);

        ConfirmationCode confirmationCode = new ConfirmationCode(email, code, expirationTime);
        confirmationCodeRepository.save(confirmationCode);

        String htmlContent = "<!DOCTYPE html>"
                + "<html lang='pt-BR'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }"
                + ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }"
                + ".header { background-color: #FFD700; color: #ffffff; padding: 10px 0; text-align: center; border-radius: 8px 8px 0 0; }"
                + ".header h1 { margin: 0; }"
                + ".content { padding: 20px; }"
                + ".content p { margin: 0 0 10px; }"
                + ".footer { text-align: center; padding: 10px 0; color: #777777; font-size: 12px; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h1>Bem-vindo à Rede Social!</h1>"
                + "</div>"
                + "<div class='content'>"
                + "<p>Olá,</p>"
                + "<p>Seja bem-vindo à nossa rede social. Prepare-se para uma experiência imersiva e envolvente!</p>"
                + "<p>Seu código de confirmação é: <strong>" + code + "</strong></p>"
                + "<p>Este código é válido por 10 minutos. Por favor, use-o para completar seu cadastro.</p>"
                + "</div>"
                + "<div class='footer'>"
                + "<p>Se você não solicitou este código, por favor ignore este e-mail.</p>"
                + "<p>&copy; 2023 Rede Social. Todos os direitos reservados.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendConfirmationEmail(email, "Código de Confirmação", htmlContent);
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao enviar e-mail de confirmação");
        }

        return ResponseEntity.ok("Código de confirmação enviado");
    }

}


