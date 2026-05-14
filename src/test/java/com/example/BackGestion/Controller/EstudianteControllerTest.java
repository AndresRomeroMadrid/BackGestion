package com.example.BackGestion.Controller;

import com.example.BackGestion.Model.Estudiante;
import com.example.BackGestion.Services.EstudianteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para EstudianteController usando MockMvc y MockBean.
 * Esta clase prueba los endpoints REST sin levantar el servidor completo.
 * 
 * COMO PROBAR:
 * 1. Desde la terminal en 'BackGestion': ./mvnw test -Dtest=EstudianteControllerTest
 * 2. Esto verificará que los endpoints responden correctamente (JSON, Status Codes, etc).
 */
@WebMvcTest(EstudianteController.class)
public class EstudianteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EstudianteService estudianteService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/estudiantes debe retornar lista de estudiantes")
    void testListar() throws Exception {
        // Datos de prueba
        Estudiante e1 = new Estudiante();
        e1.setEstudianteId(1);
        e1.setCursoId(101);

        // Mocking del servicio
        when(estudianteService.listarTodos()).thenReturn(Arrays.asList(e1));

        // Ejecutar petición y verificar
        mockMvc.perform(get("/api/estudiantes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].estudianteId").value(1))
                .andExpect(jsonPath("$[0].cursoId").value(101));
    }

    @Test
    @DisplayName("GET /api/estudiantes/{id} debe retornar 200 si el estudiante existe")
    void testObtenerPorId_Success() throws Exception {
        Estudiante e = new Estudiante();
        e.setEstudianteId(5);

        when(estudianteService.obtenerPorId(5)).thenReturn(Optional.of(e));

        mockMvc.perform(get("/api/estudiantes/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estudianteId").value(5));
    }

    @Test
    @DisplayName("POST /api/estudiantes debe crear un estudiante y retornar 200")
    void testCrear() throws Exception {
        Estudiante e = new Estudiante();
        e.setEstudianteId(10);
        e.setCursoId(200);

        when(estudianteService.guardar(any(Estudiante.class))).thenReturn(e);

        mockMvc.perform(post("/api/estudiantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(e)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estudianteId").value(10))
                .andExpect(jsonPath("$.cursoId").value(200));
    }

    @Test
    @DisplayName("DELETE /api/estudiantes/{id} debe retornar 204 No Content")
    void testEliminar() throws Exception {
        mockMvc.perform(delete("/api/estudiantes/1"))
                .andExpect(status().isNoContent());
    }
}
