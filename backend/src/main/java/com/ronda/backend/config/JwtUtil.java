package com.ronda.backend.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Genera y valida los JWT que se le devuelven al cliente.
 *
 * La clave y la expiracion se inyectan por constructor (no por campo) para que
 * los tests puedan instanciar la clase con "new" sin levantar el contexto de
 * Spring ni recurrir a reflection.
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        // HMAC-SHA256 exige una clave de al menos 256 bits (32 bytes)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String email) {
        Date ahora = new Date();
        Date vencimiento = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(ahora)
                .expiration(vencimiento)
                .signWith(key)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // Firma invalida, token expirado, formato corrupto o null
            return false;
        }
    }
}
