package app_jwt.auth_service.domain.dtos.auth;

import app_jwt.auth_service.domain.entity.Usuario;
import app_jwt.auth_service.domain.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String nombre;
    private String apellido;
    private String telefono;
    private Role role;

    public static UserResponse from(Usuario usuario) {
        return UserResponse.builder()
                .id(usuario.getId())
                .email(usuario.getCorreo())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .telefono(usuario.getTelefono())
                .role(usuario.getRole())
                .build();
    }
}