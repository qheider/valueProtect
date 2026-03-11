package info.quazi.valueProtect.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

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

        log.debug("Generating JWT token with subject: {}, expiry: {}", subject, expiryDate);
        log.debug("Using secret key length: {} chars", jwtSecret.length());

        // Convert string secret to SecretKey for JJWT 0.10+
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();

        log.debug("JWT token generated successfully (length: {} chars)", token.length());
        return token;
    }

    /**
     * Generate token for PDF processing with default subject
     */
    public String generatePdfProcessingToken() {
        return generateToken("valueprotect-pdf-processor");
    }
}
