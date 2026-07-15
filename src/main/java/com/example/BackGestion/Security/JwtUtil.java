package com.example.BackGestion.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Valida los JWT que llegan a la API. Este microservicio no emite tokens
 * (eso lo hace el servicio de autenticacion); solo verifica la firma contra
 * el secreto compartido (jwt.secret) y que el token no haya expirado.
 * El secreto se hashea con SHA-256 para obtener siempre una llave HMAC de
 * 256 bits, incluso si el secreto es corto (por ejemplo, "default-secret").
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final String SECRETO_POR_DEFECTO = "default-secret";
    private static final int LONGITUD_MINIMA_RECOMENDADA = 32;

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        if (SECRETO_POR_DEFECTO.equals(secret)) {
            log.warn("jwt.secret esta usando el valor por defecto '{}'. " +
                    "Esto es aceptable solo en desarrollo local: define JWT_SECRET en produccion.", SECRETO_POR_DEFECTO);
        } else if (secret.getBytes(StandardCharsets.UTF_8).length < LONGITUD_MINIMA_RECOMENDADA) {
            log.warn("jwt.secret tiene menos de {} bytes; se recomienda un secreto mas largo/aleatorio.", LONGITUD_MINIMA_RECOMENDADA);
        }
        this.secretKey = buildKey(secret);
    }

    private SecretKey buildKey(String secret) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hashed = sha256.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo inicializar la llave de firma JWT", e);
        }
    }

    public Claims validarYObtenerClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
