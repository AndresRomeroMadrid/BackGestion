package com.example.BackGestion.Controller;

import com.example.BackGestion.Model.Evaluacion;
import com.example.BackGestion.Repository.EvaluacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionController {

    @Autowired
    private EvaluacionRepository evaluacionRepository;

    @GetMapping("/cad/{cadId}")
    public List<Evaluacion> listarPorCad(@PathVariable Integer cadId) {
        return evaluacionRepository.findByCadId(cadId);
    }

    @PostMapping
    public Evaluacion guardar(@RequestBody Evaluacion evaluacion) {
        return evaluacionRepository.save(evaluacion);
    }

    @PutMapping("/{id}")
    public Evaluacion actualizar(@PathVariable Integer id, @RequestBody Evaluacion evaluacionDetalles) {
        Evaluacion evaluacion = evaluacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluacion no encontrada con id: " + id));
        evaluacion.setNombre(evaluacionDetalles.getNombre());
        evaluacion.setFechaEvaluacion(evaluacionDetalles.getFechaEvaluacion());
        evaluacion.setCadId(evaluacionDetalles.getCadId());
        return evaluacionRepository.save(evaluacion);
    }

    @DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        evaluacionRepository.deleteById(id);
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}
