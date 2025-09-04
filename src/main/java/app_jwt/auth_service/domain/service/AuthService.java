package app_jwt.auth_service.domain.service;

import app_jwt.auth_service.domain.dtos.auth.AuthResponse;
import app_jwt.auth_service.domain.dtos.auth.LoginRequest;
import app_jwt.auth_service.domain.dtos.auth.RegisterRequest;
import app_jwt.auth_service.domain.dtos.auth.UserResponse;
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

    @Transactional
    public AuthResponse registerEmpresa(RegisterRequest request) {
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
                .role(Role.EMPRESA)
                .build();

        usuarioRepository.save(usuario);

        String token = jwtService.getToken(usuario, usuario);

        return AuthResponse.builder()
                .token(token)
                .user(UserResponse.from(usuario))
                .build();
    }

    @Transactional
    public AuthResponse registerChofer(RegisterRequest request) {
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
                .role(Role.CHOFER)
                .build();

        usuarioRepository.save(usuario);

        String token = jwtService.getToken(usuario, usuario);

        return AuthResponse.builder()
                .token(token)
                .user(UserResponse.from(usuario))
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

            String token = jwtService.getToken(usuario, usuario);

            log.info("Login exitoso para usuario: {}", usuario.getCorreo());

            return AuthResponse.builder()
                    .token(token)
                    .user(UserResponse.from(usuario))
                    .build();

        } catch (AuthenticationException e) {
            log.error("Error de autenticación para email: {}", request.getEmail());
            throw new RuntimeException("Credenciales inválidas");
        }
    }
}