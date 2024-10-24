package rede_social.rede_social.controller.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rede_social.rede_social.dto.auth.ResponseAuthDTO;
import rede_social.rede_social.dto.auth.UserAuthDTO;
import rede_social.rede_social.dto.auth.UserRegisterDTO;
import rede_social.rede_social.dto.auth.VerifcationDTO;
import rede_social.rede_social.repository.ConfirmationCodeRepository;
import rede_social.rede_social.repository.UserRepository;
import rede_social.rede_social.service.auth.AuthService;

@RestController
@RequestMapping ("/auth")
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

    @PostMapping ("/login")
    public ResponseEntity<ResponseAuthDTO> login(@RequestBody @Valid UserAuthDTO userAuth) {
        return authService.login(userAuth);
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseAuthDTO> register(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("name") String name,
            @RequestParam("birthdate") String birthdate,
            @RequestParam("bio") String bio,
            @RequestPart("photo") MultipartFile photo) {
        UserRegisterDTO userAuth = new UserRegisterDTO(email, password, name, birthdate, photo, bio, null, null);
        return authService.register(userAuth);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestBody VerifcationDTO verifcationDTO) {
        return authService.verifyCode(verifcationDTO);
    }


}
