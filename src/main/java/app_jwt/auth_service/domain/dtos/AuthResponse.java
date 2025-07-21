package app_jwt.auth_service.domain.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;

    private UserResponse user;
    private MfaSetupResponse mfaSetup; // Para nuevos registros

    @Builder.Default
    private String type = "Bearer";
}