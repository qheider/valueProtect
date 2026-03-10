package info.quazi.valueProtect.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${pdf.processing.jwt.secret:your-super-secret-key-change-this-in-production-min-32-chars}")
    private String jwtSecret;

    @Value("${pdf.processing.jwt.algorithm:HS256}")
    private String jwtAlgorithm;

    @Value("${pdf.processing.jwt.expiration:3600000}") // Default 1 hour
    private long jwtExpiration;

    /**
     * Generate JWT token for PDF processing API authentication
     */
    public String generateToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        // Convert string secret to SecretKey for JJWT 0.10+
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Generate token for PDF processing with default subject
     */
    public String generatePdfProcessingToken() {
        return generateToken("valueprotect-pdf-processor");
    }
}
