package app_jwt.auth_service.controller;

import app_jwt.auth_service.domain.dtos.AuthResponse;
import app_jwt.auth_service.domain.dtos.LoginRequest;
import app_jwt.auth_service.domain.dtos.MfaSetupResponse;
import app_jwt.auth_service.domain.dtos.RegisterRequest;
import app_jwt.auth_service.domain.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Intento de registro para email: {}", request.getEmail());

        AuthResponse response = authService.register(request);

        log.info("Usuario registrado exitosamente: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Intento de login para email: {}", request.getEmail());

        log.debug("Request completo recibido: {}", request);
        log.debug("MFA Code específico: '{}'", request.getMfaCode());

        AuthResponse response = authService.login(request);

        log.info("Login exitoso para email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/setup-mfa")
    public ResponseEntity<MfaSetupResponse> setupMfa(@RequestParam String email) {
        log.info("Configurando MFA para email: {}", email);

        MfaSetupResponse response = authService.setupMfa(email);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<AuthResponse> verifyMfa(
            @RequestParam String email,
            @RequestParam String code) {
        log.info("Verificando código MFA para email: {}", email);

        AuthResponse response = authService.verifyAndEnableMfa(email, code);

        return ResponseEntity.ok(response);
    }
}