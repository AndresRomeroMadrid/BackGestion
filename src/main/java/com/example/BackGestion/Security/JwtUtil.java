package com.example.BackGestion.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * Valida los JWT HS256 emitidos por el servicio de autenticacion (Node/Express,
 * libreria "jsonwebtoken"). Ese servicio usa el secreto tal cual -sus bytes
 * UTF-8, sin ninguna derivacion- como clave HMAC-SHA256 y no exige una
 * longitud minima. Por eso la verificacion se hace a mano con javax.crypto en
 * vez de una libreria JWT de Java: la mayoria (jjwt incluido) rechaza con
 * WeakKeyException cualquier secreto corto como "default-secret", lo que
 * rompería la interoperabilidad con el otro servicio.
 *
 * Este microservicio no emite tokens, solo los recibe y valida.
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final String SECRETO_POR_DEFECTO = "default-secret";
    private static final int LONGITUD_MINIMA_RECOMENDADA = 32;
    private static final String ALGORITMO_HMAC = "HmacSHA256";
    private static final String ALG_ESPERADO = "HS256";

    private final byte[] secretBytes;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        if (SECRETO_POR_DEFECTO.equals(secret)) {
            log.warn("jwt.secret esta usando el valor por defecto '{}'. " +
                    "Esto es aceptable solo en desarrollo local: define JWT_SECRET en produccion.", SECRETO_POR_DEFECTO);
        } else if (secret.getBytes(StandardCharsets.UTF_8).length < LONGITUD_MINIMA_RECOMENDADA) {
            log.warn("jwt.secret tiene menos de {} bytes; se recomienda un secreto mas largo/aleatorio.", LONGITUD_MINIMA_RECOMENDADA);
        }
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Verifica firma, algoritmo y expiracion (claim "exp") del token.
     *
     * @throws JwtValidationException si el token es invalido por cualquier motivo
     */
    public Map<String, Object> validarYObtenerClaims(String token) {
        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            throw new JwtValidationException("Formato de token invalido");
        }

        String encabezadoB64 = partes[0];
        String payloadB64 = partes[1];
        String firmaB64 = partes[2];

        Map<String, Object> encabezado = parsearJson(encabezadoB64);
        if (!ALG_ESPERADO.equals(encabezado.get("alg"))) {
            throw new JwtValidationException("Algoritmo de firma no soportado");
        }

        byte[] firmaEsperada = calcularFirma(encabezadoB64 + "." + payloadB64);
        byte[] firmaRecibida = decodificarBase64Url(firmaB64);

        if (!MessageDigest.isEqual(firmaEsperada, firmaRecibida)) {
            throw new JwtValidationException("Firma invalida");
        }

        Map<String, Object> claims = parsearJson(payloadB64);
        Object exp = claims.get("exp");
        if (exp instanceof Number expNum && Instant.now().getEpochSecond() >= expNum.longValue()) {
            throw new JwtValidationException("Token expirado");
        }

        return claims;
    }

    private byte[] calcularFirma(String datos) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO_HMAC);
            mac.init(new SecretKeySpec(secretBytes, ALGORITMO_HMAC));
            return mac.doFinal(datos.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular la firma HMAC", e);
        }
    }

    private Map<String, Object> parsearJson(String base64Url) {
        try {
            byte[] json = decodificarBase64Url(base64Url);
            return objectMapper.readValue(json, Map.class);
        } catch (JwtValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtValidationException("No se pudo decodificar el token");
        }
    }

    private byte[] decodificarBase64Url(String valor) {
        try {
            return Base64.getUrlDecoder().decode(valor);
        } catch (IllegalArgumentException e) {
            throw new JwtValidationException("Token con codificacion invalida");
        }
    }
}
