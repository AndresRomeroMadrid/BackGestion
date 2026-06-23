package com.example.BackGestion.Services;

import com.example.BackGestion.Model.Estudiante;
import com.example.BackGestion.Model.Evaluacion;
import com.example.BackGestion.Model.Nota;
import com.example.BackGestion.Model.Usuario;
import com.example.BackGestion.Repository.EstudianteRepository;
import com.example.BackGestion.Repository.EvaluacionRepository;
import com.example.BackGestion.Repository.NotaRepository;
import com.example.BackGestion.dto.NotificacionNotasEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotaServiceTest {

    @Mock
    private NotaRepository notaRepository;

    @Mock
    private EvaluacionRepository evaluacionRepository;

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private SqsClient sqsClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotaService notaService;

    @BeforeEach
    void setUp() {
        // Set default values for environment and routing
        ReflectionTestUtils.setField(notaService, "exchange", "test-exchange");
        ReflectionTestUtils.setField(notaService, "routingKey", "test-routing-key");
    }

    @Test
    void guardarBulkYPublicar_EmptyList_ReturnsEmpty() {
        // Ejecución
        List<Nota> result = notaService.guardarBulkYPublicar(Collections.emptyList());

        // Verificación
        assertTrue(result.isEmpty(), "La lista devuelta debe estar vacía");
        
        // Verificamos que no se interactúe con los repositorios
        verifyNoInteractions(notaRepository);
        verifyNoInteractions(evaluacionRepository);
        verifyNoInteractions(estudianteRepository);
        verifyNoInteractions(rabbitTemplate);
        verifyNoInteractions(sqsClient);
    }

    @Test
    void guardarBulkYPublicar_DevEnvironment_SavesAndPublishesToRabbitMQ() {
        // Configuración
        ReflectionTestUtils.setField(notaService, "environment", "dev");

        Nota notaMock = new Nota();
        notaMock.setEvaluacionId(10);
        notaMock.setEstudianteId(100);
        notaMock.setValor(6.5);

        Evaluacion evaluacionMock = new Evaluacion();
        evaluacionMock.setNombre("Prueba de Matemáticas");

        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("Juan");
        usuarioMock.setApellidoPaterno("Perez");
        usuarioMock.setApellidoMaterno("Gomez");
        usuarioMock.setEmail("juan.perez@example.com");

        Estudiante estudianteMock = new Estudiante();
        estudianteMock.setUsuario(usuarioMock);

        // Simulamos comportamiento
        when(notaRepository.findByEvaluacionIdAndEstudianteId(10, 100)).thenReturn(Optional.empty());
        when(notaRepository.save(any(Nota.class))).thenReturn(notaMock);
        when(evaluacionRepository.findById(10)).thenReturn(Optional.of(evaluacionMock));
        when(estudianteRepository.findAllById(any())).thenReturn(List.of(estudianteMock));

        // Ejecución
        List<Nota> result = notaService.guardarBulkYPublicar(List.of(notaMock));

        // Verificación
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());

        // Verificar interacciones
        verify(notaRepository, times(1)).save(notaMock);
        verify(evaluacionRepository, times(1)).findById(10);
        verify(rabbitTemplate, times(1)).convertAndSend(eq("test-exchange"), eq("test-routing-key"), any(NotificacionNotasEvent.class));
        verifyNoInteractions(sqsClient); // No debe llamar a SQS en 'dev'
    }

    @Test
    void guardarOActualizarNota_ExistingNote_UpdatesValue() {
        // Configuración
        Nota notaNueva = new Nota();
        notaNueva.setEvaluacionId(10);
        notaNueva.setEstudianteId(100);
        notaNueva.setValor(7.0); // Nuevo valor

        Nota notaExistente = new Nota();
        notaExistente.setEvaluacionId(10);
        notaExistente.setEstudianteId(100);
        notaExistente.setValor(4.0); // Valor anterior

        // Simulamos comportamiento: la nota ya existe
        when(notaRepository.findByEvaluacionIdAndEstudianteId(10, 100)).thenReturn(Optional.of(notaExistente));
        when(notaRepository.save(any(Nota.class))).thenReturn(notaExistente);

        // Ejecución
        Nota result = notaService.guardarOActualizarNota(notaNueva);

        // Verificación
        assertEquals(7.0, result.getValor(), "El valor de la nota debe haber sido actualizado");
        verify(notaRepository, times(1)).findByEvaluacionIdAndEstudianteId(10, 100);
        // Debe guardar la instancia existente, pero con el valor actualizado
        verify(notaRepository, times(1)).save(notaExistente);
    }
}
