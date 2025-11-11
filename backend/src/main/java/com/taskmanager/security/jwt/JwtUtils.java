package com.taskmanager.security.jwt;

import com.taskmanager.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Objects;

import static io.jsonwebtoken.Jwts.*;
import static io.jsonwebtoken.SignatureAlgorithm.*;


@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    private static final SignatureAlgorithm JWT_ALGORITHM = HS512;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    private Key signingKey;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        if (jwtSecret != null && jwtSecret.length() > 0) {
            // Log first few characters to verify key is loaded (but not the full key for security)
            String keyPreview = jwtSecret.length() > 8 ? jwtSecret.substring(0, 8) + "..." : jwtSecret;
            logger.info("Initializing JWT utilities with secret configured: YES (length: {}, preview: {})", 
                    jwtSecret.length(), keyPreview);
        } else {
            logger.error("Initializing JWT utilities with secret configured: NO - JWT secret is missing!");
        }
        this.signingKey = generateSigningKey();
        this.jwtParser = Jwts.parser()
                .setSigningKey(signingKey)
                .build();
        logger.info("JWT utilities initialized successfully with algorithm: {}", JWT_ALGORITHM);
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(signingKey, JWT_ALGORITHM)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        Objects.requireNonNull(token, "Token cannot be null");
        return parseJwtToken(token).getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            parseJwtToken(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (SecurityException e) {
            // This catches SignatureException and other security-related exceptions
            logger.error("JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted: {}", e.getMessage());
        }
        return false;
    }

    private Key generateSigningKey() {
        if (!org.springframework.util.StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("jwt.secret is not configured");
        }

        byte[] keyBytes;
        try {
            // First try to treat the secret as Base64-encoded
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (IllegalArgumentException ex) {
            // Fallback: treat the secret as plain text
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }

        // HS512 requires >= 64 bytes of key material. If provided bytes are too short,
        // derive a strong deterministic 512-bit key from the secret using SHA-512.
        if (keyBytes.length < 64) {
            try {
                MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
                keyBytes = sha512.digest(keyBytes); // 64 bytes
                logger.warn("jwt.secret was shorter than 512 bits. Derived a strong 512-bit key via SHA-512.");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-512 algorithm not available", e);
            }
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims parseJwtToken(final String jwtToken) {
        if (!org.springframework.util.StringUtils.hasText(jwtToken)) {
            throw new IllegalArgumentException("Token cannot be null or blank");
        }
        return jwtParser.parseClaimsJws(jwtToken).getBody();
    }

}
