package com.example.BackGestion.Controller;

import com.example.BackGestion.Model.Nota;
import com.example.BackGestion.Services.NotaService;
import com.example.BackGestion.dto.NotaEstudianteProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notas")
@CrossOrigin(origins = "*")
public class NotaController {

    @Autowired
    private NotaService notaService;

    @Autowired
    private com.example.BackGestion.Repository.NotaRepository notaRepository;

    @PostMapping
    public Nota guardar(@RequestBody Nota nota) {
        return notaService.guardarOActualizarNota(nota);
    }

    @PostMapping("/bulk")
    public List<Nota> guardarVarias(@RequestBody List<Nota> notas) {
        return notaService.guardarBulkYPublicar(notas);
    }

    @GetMapping
    public List<Nota> listar() {
        return notaService.listarTodas();
    }

    @GetMapping("/estudiante/{id}")
    public List<NotaEstudianteProjection> getNotasByEstudiante(@PathVariable Integer id) {
        return notaRepository.findNotasByEstudianteId(id);
    }

    @GetMapping("/curso/{cursoId}/asignatura/{asignaturaId}")
    public List<NotaEstudianteProjection> obtenerPorCursoYAsignatura(
            @PathVariable Integer cursoId,
            @PathVariable Integer asignaturaId) {
        return notaService.obtenerNotasPorCursoYAsignatura(cursoId, asignaturaId);
    }
}
