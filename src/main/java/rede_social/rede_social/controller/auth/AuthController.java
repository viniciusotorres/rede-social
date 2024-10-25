package rede_social.rede_social.controller.auth;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rede_social.rede_social.dto.auth.ResponseAuthDTO;
import rede_social.rede_social.dto.auth.UserAuthDTO;
import rede_social.rede_social.dto.auth.UserRegisterDTO;
import rede_social.rede_social.dto.auth.VerifcationDTO;
import rede_social.rede_social.dto.email.PasswordResetResponse;
import rede_social.rede_social.repository.ConfirmationCodeRepository;
import rede_social.rede_social.repository.UserRepository;
import rede_social.rede_social.service.auth.AuthService;

import java.io.IOException;

/**
 * Controlador responsável pela autenticação de usuários.
 */
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final AuthService authService;
    private final ConfirmationCodeRepository confirmationCodeRepository;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, ConfirmationCodeRepository confirmationCodeRepository, UserRepository userRepository) {
        this.authService = authService;
        this.confirmationCodeRepository = confirmationCodeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Realiza o login de um usuário.
     *
     * @param userAuth DTO com as informações de autenticação do usuário.
     * @return ResponseEntity com informações do usuário e token de autenticação.
     */
    @PostMapping("/login")
    public ResponseEntity<ResponseAuthDTO> login(@RequestBody @Valid UserAuthDTO userAuth) {
        return authService.login(userAuth);
    }

    /**
     * Registra um novo usuário.
     *
     * @param photo Arquivo de imagem do usuário.
     * @return ResponseEntity com informações do novo usuário e token de autenticação.
     * @throws IOException Se houver erro ao processar a imagem.
     */
    @PostMapping("/register")
    public ResponseEntity<ResponseAuthDTO> register(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("name") String name,
            @RequestParam("birthdate") String birthdate,
            @RequestParam("bio") String bio,
            @RequestPart("photo") MultipartFile photo) throws IOException, MessagingException {

        UserRegisterDTO userAuth = new UserRegisterDTO(email, password, name, birthdate, photo, bio, null, null);
        return authService.register(userAuth);
    }

    /**
     * Verifica o código de confirmação enviado por e-mail.
     *
     * @param verificationDTO DTO contendo o e-mail e o código a ser verificado.
     * @return ResponseEntity com a mensagem de confirmação ou erro.
     */
    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestBody @Valid VerifcationDTO verificationDTO) throws MessagingException {
        return authService.verifyCode(verificationDTO);
    }

    /**
     * Solicita o envio de um e-mail para recuperação de senha.
     *
     * @param email E-mail do usuário que deseja recuperar a senha.
     * @return ResponseEntity com mensagem de sucesso ou erro.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody @Valid String email) {
        return authService.forgotPassword(email);
    }

    /**
     * Redefine a senha do usuário.
     *
     * @param email E-mail do usuário.
     * @param password Nova senha do usuário.
     * @param code Código de confirmação para redefinição de senha.
     * @return ResponseEntity com a mensagem de sucesso ou erro.
     * @throws MessagingException Se houver erro ao enviar o e-mail de confirmação.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("code") String code) throws MessagingException {
        return authService.resetPassword(email, password, code);
    }

    /**
     *  Envia novamente o código de confirmação para o email
     * @param email email do usuário que deseja receber o código
     * @return ResponseEntity com a mensagem de sucesso ou erro ao enviar o código
     * @throws MessagingException
     */
    @PostMapping("/send-again-code")
    public ResponseEntity<String> sendAgainCode(@RequestBody @Valid String email) throws MessagingException {
        return authService.sendAgainCode(email);
    }
}
