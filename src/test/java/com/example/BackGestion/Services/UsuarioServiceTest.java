package com.example.BackGestion.Services;

import com.example.BackGestion.Model.Usuario;
import com.example.BackGestion.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para UsuarioService usando Mockito.
 * 
 * COMO PROBAR:
 * 1. Desde la terminal en la raíz de 'BackGestion', ejecuta: ./mvnw test
 * 2. Para probar solo esta clase: ./mvnw test -Dtest=UsuarioServiceTest
 * 3. En VS Code: Haz clic en 'Run Test' sobre el nombre de la clase o método.
 */
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        // Inicializa los mocks anotados con @Mock
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe listar todos los usuarios")
    void testObtenerTodos() {
        // Mock de datos
        Usuario u1 = new Usuario();
        u1.setNombre("Juan");
        Usuario u2 = new Usuario();
        u2.setNombre("Maria");

        // Definir comportamiento del Mock
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        // Ejecutar método
        List<Usuario> result = usuarioService.obtenerTodos();

        // Verificaciones
        assertEquals(2, result.size());
        assertEquals("Juan", result.get(0).getNombre());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener un usuario por ID exitosamente")
    void testObtenerPorId_Success() {
        Usuario u = new Usuario();
        u.setUsuarioId(1);
        u.setNombre("Test");

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(u));

        Usuario result = usuarioService.obtenerPorId(1);

        assertNotNull(result);
        assertEquals(1, result.getUsuarioId());
        assertEquals("Test", result.getNombre());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no existe")
    void testObtenerPorId_NotFound() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            usuarioService.obtenerPorId(99);
        });
    }

    @Test
    @DisplayName("Debe actualizar un usuario correctamente")
    void testActualizar() {
        // Usuario existente en DB
        Usuario existing = new Usuario();
        existing.setUsuarioId(1);
        existing.setNombre("Nombre Viejo");
        existing.setEmail("viejo@test.com");

        // Datos nuevos para actualizar
        Usuario details = new Usuario();
        details.setNombre("Nombre Nuevo");
        details.setEmail("nuevo@test.com");

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existing));
        // Mock de save: devuelve el objeto que recibe
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArguments()[0]);

        Usuario result = usuarioService.actualizar(1, details);

        assertEquals("Nombre Nuevo", result.getNombre());
        assertEquals("nuevo@test.com", result.getEmail());
        verify(usuarioRepository).save(existing);
    }
}
