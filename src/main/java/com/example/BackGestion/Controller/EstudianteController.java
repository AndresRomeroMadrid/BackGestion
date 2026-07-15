package com.example.BackGestion.Controller;

import com.example.BackGestion.Services.EstudianteService;
import com.example.BackGestion.dto.EstudianteCursoProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    @Autowired
    private EstudianteService estudianteService;

    @GetMapping("/curso/{cursoId}")
    public List<EstudianteCursoProjection> obtenerPorCurso(@PathVariable Integer cursoId) {
        return estudianteService.obtenerEstudiantesPorCurso(cursoId);
    }

    @GetMapping
    public List<com.example.BackGestion.Model.Estudiante> listar() {
        return estudianteService.listarTodos();
    }

    @GetMapping("/{id}")
    public com.example.BackGestion.Model.Estudiante obtenerPorId(@PathVariable Integer id) {
        return estudianteService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con id: " + id));
    }

    @PostMapping
    public com.example.BackGestion.Model.Estudiante crear(@RequestBody com.example.BackGestion.Model.Estudiante estudiante) {
        return estudianteService.guardar(estudiante);
    }

    @DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        estudianteService.eliminar(id);
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public com.example.BackGestion.Model.Estudiante actualizar(@PathVariable Integer id, @RequestBody com.example.BackGestion.Model.Estudiante estudianteDetalles) {
        com.example.BackGestion.Model.Estudiante estudiante = estudianteService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        estudiante.setCursoId(estudianteDetalles.getCursoId());
        return estudianteService.guardar(estudiante);
    }
}
