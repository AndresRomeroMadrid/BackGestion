package com.example.BackGestion.Controller;

import com.example.BackGestion.Model.Anotacion;
import com.example.BackGestion.Repository.AnotacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anotaciones")
public class AnotacionController {

    @Autowired
    private AnotacionRepository anotacionRepository;

    @PostMapping
    public Anotacion guardar(@RequestBody Anotacion anotacion) {
        if (anotacion.getFechaRegistro() == null) {
            anotacion.setFechaRegistro(java.time.LocalDateTime.now());
        }
        return anotacionRepository.save(anotacion);
    }

    @GetMapping("/estudiante/{estudianteId}")
    public List<Anotacion> obtenerPorEstudiante(@PathVariable Integer estudianteId) {
        return anotacionRepository.findByEstudianteIdOrderByFechaRegistroDesc(estudianteId);
    }

    @GetMapping
    public List<Anotacion> listar() {
        return anotacionRepository.findAll();
    }
}
