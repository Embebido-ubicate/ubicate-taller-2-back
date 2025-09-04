package app_jwt.auth_service.domain.dtos.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private UserResponse user;

    @Builder.Default
    private String type = "Bearer";
}