package com.example.BackGestion.Services;

import com.example.BackGestion.Model.Nota;
import com.example.BackGestion.Repository.NotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas para NotaService.
 * Se prueba la lógica de 'Guardar o Actualizar'.
 */
public class NotaServiceTest {

    @Mock
    private NotaRepository notaRepository;

    @InjectMocks
    private NotaService notaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe actualizar la nota si ya existe")
    void testGuardarOActualizar_Update() {
        // Datos de entrada
        Nota notaInput = new Nota();
        notaInput.setEvaluacionId(1);
        notaInput.setEstudianteId(10);
        notaInput.setValor(6.5);

        // Nota existente en DB
        Nota notaExistente = new Nota();
        notaExistente.setNotaId(100);
        notaExistente.setValor(4.0);

        // Mocking: findBy... retorna la nota existente
        when(notaRepository.findByEvaluacionIdAndEstudianteId(1, 10))
                .thenReturn(Optional.of(notaExistente));
        when(notaRepository.save(any(Nota.class))).thenAnswer(i -> i.getArguments()[0]);

        Nota result = notaService.guardarOActualizarNota(notaInput);

        // Verificaciones
        assertEquals(6.5, result.getValor());
        assertEquals(100, result.getNotaId()); // Conserva el ID original
        verify(notaRepository, times(1)).save(notaExistente);
    }

    @Test
    @DisplayName("Debe crear una nota nueva si no existe")
    void testGuardarOActualizar_SaveNew() {
        Nota notaInput = new Nota();
        notaInput.setEvaluacionId(1);
        notaInput.setEstudianteId(10);
        notaInput.setValor(7.0);

        // Mocking: no encuentra nada
        when(notaRepository.findByEvaluacionIdAndEstudianteId(1, 10))
                .thenReturn(Optional.empty());
        when(notaRepository.save(any(Nota.class))).thenAnswer(i -> i.getArguments()[0]);

        Nota result = notaService.guardarOActualizarNota(notaInput);

        assertEquals(7.0, result.getValor());
        verify(notaRepository, times(1)).save(notaInput);
    }
}
