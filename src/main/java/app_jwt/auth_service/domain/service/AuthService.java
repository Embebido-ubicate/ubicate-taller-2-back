package app_jwt.auth_service.domain.service;

import app_jwt.auth_service.domain.dtos.*;
import app_jwt.auth_service.domain.entity.Usuario;
import app_jwt.auth_service.domain.enums.Role;
import app_jwt.auth_service.infra.repository.UsuarioRepository;
import app_jwt.auth_service.infra.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final MFAService mfaService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.findByCorreo(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }
        if (usuarioRepository.findByUsername(request.getEmail()).isPresent()) {
            throw new RuntimeException("El usuario ya existe");
        }

        Usuario usuario = Usuario.builder()
                .username(request.getEmail())
                .correo(request.getEmail())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .telefono(request.getTelefono())
                .dni(request.getDni())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .totpSecret(null)
                .mfaEnabled(false)
                .build();

        usuarioRepository.save(usuario);

        String token = jwtService.getToken(usuario, usuario);

        return AuthResponse.builder()
                .token(token)
                .user(UserResponse.from(usuario))
                .mfaSetup(null)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {

            Usuario usuario = usuarioRepository.findByCorreo(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            usuario.getUsername(),
                            request.getPassword()
                    )
            );
            if (Boolean.TRUE.equals(usuario.getMfaEnabled())) {

                if (request.getMfaCode() == null || request.getMfaCode().isEmpty()) {
                    log.warn("Código MFA requerido pero no proporcionado para: {}", usuario.getCorreo());
                    throw new RuntimeException("Código MFA requerido");
                }

                if (request.getMfaCode().length() != 6) {
                    log.error("Código MFA tiene longitud incorrecta: {} (esperado: 6)", request.getMfaCode().length());
                    throw new RuntimeException("Código MFA debe tener 6 dígitos");
                }

                if (!request.getMfaCode().matches("\\d{6}")) {
                    log.error("Código MFA contiene caracteres no numéricos: {}", request.getMfaCode());
                    throw new RuntimeException("Código MFA debe contener solo números");
                }

                if (!mfaService.validateTOTP(usuario.getTotpSecret(), request.getMfaCode())) {
                    log.error("Código MFA inválido: {} para usuario: {}", request.getMfaCode(), usuario.getCorreo());
                    throw new RuntimeException("Código MFA inválido");
                }

            } else {
                log.debug("MFA no habilitado para: {}, login directo", usuario.getCorreo());
            }

            String token = jwtService.getToken(usuario, usuario);

            return AuthResponse.builder()
                    .token(token)
                    .user(UserResponse.from(usuario))
                    .build();

        } catch (AuthenticationException e) {
            log.error("Error de autenticación para email: {}", request.getEmail());
            throw new RuntimeException("Credenciales inválidas");
        }
    }

    public MfaSetupResponse setupMfa(String email) {
        Usuario usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (Boolean.TRUE.equals(usuario.getMfaEnabled())) {
            throw new RuntimeException("MFA ya está habilitado para este usuario");
        }

        String secret = mfaService.generateSecret();
        String qrCodeImage = generateQRCodeForUser(usuario.getUsername(), secret);

        usuario.setTotpSecret(secret);
        usuario.setMfaEnabled(false);
        usuarioRepository.save(usuario);

        return MfaSetupResponse.builder()
                .qrCodeImage(qrCodeImage)
                .secret(secret)
                .qrCodeUrl(mfaService.generateQRCodeURL(usuario.getUsername(), secret))
                .message("Escanea el código QR y luego verifica con un código para habilitar MFA")
                .build();
    }

    @Transactional
    public AuthResponse verifyAndEnableMfa(String email, String code) {
        Usuario usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getTotpSecret() == null || usuario.getTotpSecret().isEmpty()) {
            throw new RuntimeException("MFA no configurado para este usuario");
        }

        if (!mfaService.validateTOTP(usuario.getTotpSecret(), code)) {
            usuario.setTotpSecret(null);
            usuario.setMfaEnabled(false);
            usuarioRepository.save(usuario);
            throw new RuntimeException("Código MFA inválido");
        }

        usuario.setMfaEnabled(true);
        usuarioRepository.save(usuario);

        log.info("MFA verificado y habilitado para usuario: {} - Estado: {}",
                email, usuario.getMfaEnabled());

        String token = jwtService.getToken(usuario, usuario);

        return AuthResponse.builder()
                .token(token)
                .user(UserResponse.from(usuario))
                .build();
    }

    private String generateQRCodeForUser(String username, String secret) {
        try {
            return mfaService.generateQRCodeImage(username, secret);
        } catch (Exception e) {
            log.error("Error generando código QR para usuario: {}", username, e);
            throw new RuntimeException("Error generando código QR");
        }
    }
}