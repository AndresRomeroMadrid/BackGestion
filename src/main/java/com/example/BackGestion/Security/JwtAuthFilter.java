package com.example.BackGestion.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Filtro que se ejecuta en cada peticion HTTP y valida que traiga un JWT
 * valido en el header Authorization, salvo para las rutas publicas
 * (login, Swagger/OpenAPI y preflight CORS).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER_AUTORIZACION = "Authorization";
    private static final String PREFIJO_BEARER = "Bearer ";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    // Patrones tipo Ant (segment-aware) para evitar bypass por prefijo
    // (p. ej. que "/api/usuarios/loginX" cuele como ruta publica).
    private static final List<String> PATRONES_PUBLICOS = List.of(
            "/api/usuarios/login",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**"
    );

    // Rutas publicas que solo aplican para GET (no cubren POST/DELETE del mismo path)
    private static final List<String> PATRONES_PUBLICOS_GET = List.of(
            "/api/academico/cursos"
    );

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        // getServletPath() ya viene decodificado y normalizado por el contenedor
        // (sin ".." ni "//"), a diferencia de getRequestURI().
        String path = request.getServletPath();
        if (PATRONES_PUBLICOS.stream().anyMatch(patron -> PATH_MATCHER.match(patron, path))) {
            return true;
        }
        return HttpMethod.GET.matches(request.getMethod())
                && PATRONES_PUBLICOS_GET.stream().anyMatch(patron -> PATH_MATCHER.match(patron, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HEADER_AUTORIZACION);

        if (header == null || !header.startsWith(PREFIJO_BEARER)) {
            rechazar(response, "Token no proporcionado");
            return;
        }

        String token = header.substring(PREFIJO_BEARER.length());

        try {
            jwtUtil.validarYObtenerClaims(token);
        } catch (JwtValidationException e) {
            rechazar(response, "Token invalido o expirado");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void rechazar(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("error", mensaje)));
    }
}
