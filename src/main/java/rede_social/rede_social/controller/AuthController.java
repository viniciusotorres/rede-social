package rede_social.rede_social.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rede_social.rede_social.dto.auth.ResponseAuthDTO;
import rede_social.rede_social.dto.auth.UserAuthDTO;
import rede_social.rede_social.dto.auth.UserRegisterDTO;
import rede_social.rede_social.service.auth.AuthService;

@RestController
@RequestMapping ("/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping ("/login")
    public ResponseEntity<ResponseAuthDTO> login(@RequestBody @Valid UserAuthDTO userAuth) {
        return authService.login(userAuth);
    }

    @PostMapping ("/register")
    public ResponseEntity<ResponseAuthDTO> register(@RequestBody @Valid UserRegisterDTO userAuth) {
        return authService.register(userAuth);
    }
}
