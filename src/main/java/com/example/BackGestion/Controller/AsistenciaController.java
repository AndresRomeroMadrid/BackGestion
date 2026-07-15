package com.example.BackGestion.Controller;

import com.example.BackGestion.Model.Asistencia;
import com.example.BackGestion.Repository.AsistenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @PostMapping("/bulk")
    public List<Asistencia> guardarMultiple(@RequestBody List<Asistencia> asistencias) {
        return asistenciaRepository.saveAll(asistencias);
    }

    @GetMapping
    public List<Asistencia> listar() {
        return asistenciaRepository.findAll();
    }

    @GetMapping("/estudiante/{id}")
    public List<com.example.BackGestion.dto.AsistenciaResumenProjection> getAsistenciaPorEstudiante(@PathVariable Integer id) {
        return asistenciaRepository.findAsistenciaResumenByEstudianteId(id);
    }
}
