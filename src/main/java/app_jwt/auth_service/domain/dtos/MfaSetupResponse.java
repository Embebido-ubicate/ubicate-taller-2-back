package app_jwt.auth_service.domain.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MfaSetupResponse {
    private String qrCodeImage;
    private String secret;
    private String qrCodeUrl;
    private String message;
}