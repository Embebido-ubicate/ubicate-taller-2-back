package app_jwt.auth_service.domain.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "El email es obligatorio")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @JsonProperty("password")
    private String password;

    @Pattern(regexp = "^$|\\d{6}", message = "El código MFA debe tener 6 dígitos o estar vacío")
    @JsonProperty("mfaCode")
    private String mfaCode;
}
