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
import rede_social.rede_social.dto.email.PasswordResetResponse;
import rede_social.rede_social.exception.authException.InvalidCredentialsException;
import rede_social.rede_social.exception.authException.UserAlreadyRegisteredException;
import rede_social.rede_social.exception.authException.UserNotFoundException;
import rede_social.rede_social.exception.authException.UserNotVerifiedException;
import rede_social.rede_social.model.ConfirmationCode;
import rede_social.rede_social.model.User;
import rede_social.rede_social.repository.ConfirmationCodeRepository;
import rede_social.rede_social.repository.UserRepository;
import rede_social.rede_social.service.email.EmailService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Serviço responsável pela autenticação de usuários, incluindo registro,
 * login, verificação de código, recuperação de senha e envio de e-mails.
 */
@Service
public class AuthService {
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final ConfirmationCodeRepository confirmationCodeRepository;

    /**
     * Construtor do serviço de autenticação.
     *
     * @param passwordEncoder                 Codificador de senhas.
     * @param tokenService                    Serviço para geração de tokens.
     * @param userRepository                  Repositório de usuários.
     * @param emailService                    Serviço de e-mail.
     * @param confirmationCodeRepository      Repositório de códigos de confirmação.
     */
    public AuthService(PasswordEncoder passwordEncoder, TokenService tokenService,
                       UserRepository userRepository, EmailService emailService,
                       ConfirmationCodeRepository confirmationCodeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.confirmationCodeRepository = confirmationCodeRepository;
    }

    /**
     * Realiza o login de um usuário.
     *
     * @param userAuth DTO contendo informações de autenticação do usuário.
     * @return Resposta contendo o token gerado e informações do usuário.
     * @throws UserNotFoundException          Se o usuário não for encontrado.
     * @throws UserNotVerifiedException        Se o usuário não estiver verificado.
     * @throws InvalidCredentialsException      Se as credenciais forem inválidas.
     */
    @Transactional
    public ResponseEntity<ResponseAuthDTO> login(UserAuthDTO userAuth) {
        var user = userRepository.findByEmail(userAuth.email())
                .orElseThrow(() -> {
                    logger.info("Tentativa de login para o usuário: " + userAuth.email());
                    return new UserNotFoundException(userAuth.email());
                });

        if (!user.isVerified()) {
            logger.info("Usuário não verificado: " + user.getName());
            throw new UserNotVerifiedException(user.getName());
        }

        if (user.isPasswordValid(userAuth.password(), passwordEncoder)) {
            var token = tokenService.generateToken(user);
            logger.info("Login efetuado com sucesso para o usuário: " + user.getName());
            return ResponseEntity.ok(new ResponseAuthDTO(token, user.getEmail(), "Usuário logado com sucesso", user.getId()));
        }

        logger.info("Credenciais inválidas para o usuário: " + user.getName());
        throw new InvalidCredentialsException();
    }

    /**
     * Registra um novo usuário.
     *
     * @param userRegister DTO contendo informações de registro do usuário.
     * @return Resposta contendo o token gerado e informações do usuário.
     * @throws IOException                        Se ocorrer um erro ao salvar a imagem.
     * @throws UserAlreadyRegisteredException     Se o usuário já estiver registrado.
     */
    @Transactional
    public ResponseEntity<ResponseAuthDTO> register(UserRegisterDTO userRegister) throws IOException, MessagingException {
        checkUserAlreadyRegistered(userRegister.email());

        User user = createUser(userRegister);
        String imageBase64 = saveUserImage(userRegister.photo());
        user.setPhoto(imageBase64.getBytes());
        user.setVerified(false);
        userRepository.save(user);

        ConfirmationCode confirmationCode = createConfirmationCode(userRegister.email());
        confirmationCodeRepository.save(confirmationCode);
        sendCode(userRegister.email());

        String token = tokenService.generateToken(user);

        logger.info("Usuário registrado com sucesso: " + userRegister.email());

        return ResponseEntity.ok(new ResponseAuthDTO(token, userRegister.email(), "Usuário registrado com sucesso", user.getId()));
    }

    /**
     * Verifica o código de confirmação enviado para o usuário.
     *
     * @param verificationDTO contendo o e-mail e o código de verificação.
     * @return Mensagem de sucesso ou falha da verificação.
     */
    @Transactional
    public ResponseEntity<String> verifyCode(VerifcationDTO verificationDTO) throws MessagingException {
        // Busca o código de confirmação
        var confirmationCode = confirmationCodeRepository.findById(verificationDTO.email())
                .orElseThrow(() -> new RuntimeException("Código de confirmação não encontrado"));

        // Verifica se o código é válido e não expirou
        if (!confirmationCode.getCode().equals(verificationDTO.code()) ||
                confirmationCode.getExpirationDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Código de confirmação inválido ou expirado");
        }

        // Busca o usuário associado ao e-mail
        var user = userRepository.findByEmail(verificationDTO.email())
                .orElseThrow(() -> new UserNotFoundException(verificationDTO.email()));

        // Marca o usuário como verificado
        user.setVerified(true);
        userRepository.save(user);

        // Envia o e-mail de confirmação
        sendSuccessRegisterEmail(verificationDTO.email());

        return ResponseEntity.ok("Código de confirmação válido");
    }

    /**
     * Envia novamente o código de confirmação para o e-mail do usuário.
     * @param email E-mail do usuário que receberá o código.
     * @return Mensagem indicando o status do envio do código.
     * @throws MessagingException
     */
    public ResponseEntity<String> sendAgainCode(String email) throws MessagingException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (user.isVerified()) {
            return ResponseEntity.badRequest().body("Usuário já verificado");
        }

        var confirmationCode = confirmationCodeRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("Código de confirmação não encontrado"));

        if (confirmationCode.getExpirationDate().isBefore(LocalDateTime.now())) {
            confirmationCodeRepository.delete(confirmationCode);
            confirmationCode = createConfirmationCode(email);
            confirmationCodeRepository.save(confirmationCode);
        }

        sendCode(email);

        return ResponseEntity.ok("Código de confirmação enviado");
    }

    /**
     * Envia um código de confirmação para o e-mail do usuário.
     *
     * @param email E-mail do usuário que receberá o código.
     * @return Mensagem indicando o status do envio do código.
     */
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

        String htmlContent = generateEmailContent(
                "Código de Confirmação",
                "Seu código de confirmação é: <strong>" + code + "</strong><br/>" +
                        "Este código é válido por 10 minutos. Por favor, use-o para completar seu cadastro.",
                "Se você não solicitou este código, por favor ignore este e-mail. &copy; 2023 Rede Social. Todos os direitos reservados.");

        try {
            emailService.sendConfirmationEmail(email, "Código de Confirmação", htmlContent);
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao enviar e-mail de confirmação");
        }

        return ResponseEntity.ok("Código de confirmação enviado");
    }

    /**
     * Envia um e-mail para recuperação de senha.
     *
     * @param email E-mail do usuário que solicitará a recuperação de senha.
     * @return Mensagem indicando o status do envio do e-mail.
     */
    public ResponseEntity<String> forgotPassword(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!user.isVerified()) {
            return ResponseEntity.badRequest().body("Usuário não verificado");
        }

        String code = CodeGenerator.generateCode();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(10);
        ConfirmationCode confirmationCode = new ConfirmationCode(email, code, expirationTime);
        confirmationCodeRepository.save(confirmationCode);

        try {
            emailService.sendConfirmationEmail(email, "Recuperação de Senha", generateForgotPasswordEmailContent(code));
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao enviar e-mail de recuperação de senha");
        }

        return ResponseEntity.ok("E-mail de recuperação de senha enviado");
    }

    /**
     * Redefine a senha do usuário com base no e-mail e código fornecidos.
     *
     * @param email   E-mail do usuário.
     * @param password Nova senha do usuário.
     * @param code    Código de confirmação.
     * @return Mensagem indicando o resultado da redefinição da senha.
     * @throws MessagingException Se ocorrer um erro ao enviar o e-mail de sucesso.
     */
    public ResponseEntity<PasswordResetResponse> resetPassword(String email, String password, String code) throws MessagingException {
        var confirmationCode = confirmationCodeRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("Código de confirmação não encontrado"));

        if (isCodeValid(confirmationCode, code)) {
            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            updatePassword(user, password);
            confirmationCodeRepository.delete(confirmationCode);

            sendSuccessEmail(email);

            return ResponseEntity.ok(new PasswordResetResponse("Senha redefinida com sucesso"));
        } else {
            return ResponseEntity.badRequest().body(new PasswordResetResponse("Código de confirmação inválido ou expirado"));
        }
    }

    // Métodos privados

    private boolean isCodeValid(ConfirmationCode confirmationCode, String code) {
        return confirmationCode.getCode().equals(code) && confirmationCode.getExpirationDate().isAfter(LocalDateTime.now());
    }

    private void updatePassword(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    private void sendSuccessRegisterEmail(String email) throws MessagingException {
        String title = "Cadastro Realizado com Sucesso";
        String message = "Seu cadastro foi validado com sucesso. Agora você pode acessar sua conta com seu e-mail e senha.<br>";
        String footer = "&copy; 2023 Rede Social. Todos os direitos reservados.";

        String htmlContent = generateEmailContent(title, message, footer);
        emailService.sendConfirmationEmail(email, title, htmlContent);
    }

    private void sendSuccessEmail(String email) throws MessagingException {
        String title = "Senha Alterada com Sucesso";
        String message = "Sua senha foi alterada com sucesso. Agora você pode acessar sua conta com sua nova senha.<br>"
                + "Se você não fez essa alteração, por favor entre em contato com o suporte imediatamente.";
        String footer = "&copy; 2023 Rede Social. Todos os direitos reservados.";

        String htmlContent = generateEmailContent(title, message, footer);
        emailService.sendConfirmationEmail(email, title, htmlContent);
    }

    private String generateForgotPasswordEmailContent(String code) {
        String title = "Recuperação de Senha";
        String message = "Recebemos uma solicitação para redefinir sua senha. Se você não fez essa solicitação, por favor ignore este e-mail e sua senha permanecerá a mesma.<br>"
                + "Se você fez essa solicitação, use o código abaixo para redefinir sua senha:<br>"
                + "<strong>" + code + "</strong><br>"
                + "Este código é válido por 10 minutos.";
        String footer = "&copy; 2023 Rede Social. Todos os direitos reservados.";

        return generateEmailContent(title, message, footer);
    }

    private String generateEmailContent(String title, String message, String footer) {
        return "<!DOCTYPE html>"
                + "<html lang='pt-BR'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }"
                + ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }"
                + ".header { background-color: #4CAF50; color: #ffffff; padding: 10px 0; text-align: center; border-radius: 8px 8px 0 0; }"
                + ".header h1 { margin: 0; }"
                + ".content { padding: 20px; }"
                + ".content p { margin: 0 0 10px; }"
                + ".footer { text-align: center; padding: 10px 0; color: #777777; font-size: 12px; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h1>" + title + "</h1>"
                + "</div>"
                + "<div class='content'>"
                + "<p>" + message + "</p>"
                + "</div>"
                + "<div class='footer'>"
                + "<p>" + footer + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    private boolean isValidImageType(MultipartFile image) {
        String contentType = image.getContentType();
        return contentType.equals("image/jpeg") || contentType.equals("image/jpg") || contentType.equals("image/png");
    }

    private String saveImage(MultipartFile image) throws IOException {
        byte[] bytes = image.getBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private void checkUserAlreadyRegistered(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            logger.info("Usuário já registrado: " + email);
            throw new UserAlreadyRegisteredException(email);
        }
    }

    private User createUser(UserRegisterDTO userRegister) throws IOException {
        User user = new User();
        user.updateFromDTO(userRegister, passwordEncoder);
        return user;
    }

    private String saveUserImage(MultipartFile image) {
        if (!isValidImageType(image)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de imagem inválido. Apenas JPG, JPEG e PNG são permitidos.");
        }
        try {
            return saveImage(image);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar a imagem.");
        }
    }

    private ConfirmationCode createConfirmationCode(String email) {
        String code = CodeGenerator.generateCode();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(10);
        return new ConfirmationCode(email, code, expirationTime);
    }
}
