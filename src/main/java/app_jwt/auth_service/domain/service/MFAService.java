package app_jwt.auth_service.domain.service;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@Slf4j
public class MFAService {

    @Value("${mfa.issuer}")
    private String issuer;

    private final DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator(
            dev.samstevens.totp.code.HashingAlgorithm.SHA1, 6);
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(
            codeGenerator, new SystemTimeProvider());
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();

    public String generateSecret() {
        String secret = secretGenerator.generate();
        log.debug("Secret generado para MFA");
        return secret;
    }

    public String generateQRCodeURL(String username, String secret) {
        String label = issuer + ":" + username;
        String url = String.format(
                "otpauth://totp/%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                label, secret, issuer
        );

        return "https://www.google.com/chart?chs=200x200&chld=M&cht=qr&chl=" +
                java.net.URLEncoder.encode(url, java.nio.charset.StandardCharsets.UTF_8);
    }

    public String generateQRCodeImage(String username, String secret) {
        try {
            QrData data = new QrData.Builder()
                    .label(username)
                    .secret(secret)
                    .issuer(issuer)
                    .algorithm(dev.samstevens.totp.code.HashingAlgorithm.SHA1)
                    .digits(6)
                    .period(30)
                    .build();

            byte[] imageData = qrGenerator.generate(data);
            String base64Image = Base64.getEncoder().encodeToString(imageData);

            log.debug("Código QR generado para usuario: {}", username);
            return "data:image/png;base64," + base64Image;

        } catch (Exception e) {
            log.error("Error generando código QR para usuario: {}", username, e);
            throw new RuntimeException("Error generando código QR", e);
        }
    }

    public boolean validateTOTP(String secret, String code) {
        if (secret == null || secret.isEmpty() || code == null || code.isEmpty()) {
            log.warn("Secret o código TOTP vacío");
            return false;
        }

        try {
            boolean isValid = codeVerifier.isValidCode(secret, code);
            log.debug("Validación TOTP: {}", isValid ? "exitosa" : "fallida");
            return isValid;
        } catch (Exception e) {
            log.error("Error validando código TOTP", e);
            return false;
        }
    }
}