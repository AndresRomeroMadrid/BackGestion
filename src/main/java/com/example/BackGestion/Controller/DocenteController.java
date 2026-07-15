package com.example.BackGestion.Controller;

import com.example.BackGestion.Model.Docente;
import com.example.BackGestion.Services.DocenteService;
import com.example.BackGestion.dto.DocenteCursoProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    @Autowired
    private DocenteService docenteService;

    @GetMapping
    public List<Docente> listar() {
        return docenteService.listarDocentes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Docente> obtener(@PathVariable Integer id) {
        return docenteService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/cursos")
    public List<DocenteCursoProjection> obtenerCursos(@PathVariable Integer id) {
        return docenteService.obtenerCursosPorDocente(id);
    }

    @PostMapping
    public Docente crear(@RequestBody Docente docente) {
        return docenteService.guardar(docente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        docenteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cad/all")
    public List<DocenteCursoProjection> getTodosLosCads() {
        return docenteService.listarTodosLosCads();
    }
}
