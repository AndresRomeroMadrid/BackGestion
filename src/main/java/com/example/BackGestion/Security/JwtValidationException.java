package com.example.BackGestion.Security;

/**
 * Se lanza cuando un JWT recibido no es valido: firma incorrecta,
 * formato invalido, algoritmo no soportado o token expirado.
 */
public class JwtValidationException extends RuntimeException {
    public JwtValidationException(String message) {
        super(message);
    }
}
