package com.example.BackGestion.Services;

import com.example.BackGestion.Model.Estudiante;
import com.example.BackGestion.Model.Nota;
import com.example.BackGestion.Model.Usuario;
import com.example.BackGestion.Repository.EstudianteRepository;
import com.example.BackGestion.Repository.EvaluacionRepository;
import com.example.BackGestion.Repository.NotaRepository;
import com.example.BackGestion.dto.NotaEstudianteProjection;
import com.example.BackGestion.dto.NotificacionNotasEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotaService {

    @Autowired
    private NotaRepository notaRepository;

    @Autowired
    private EvaluacionRepository evaluacionRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    public Nota guardarOActualizarNota(Nota nota) {
        return notaRepository.findByEvaluacionIdAndEstudianteId(nota.getEvaluacionId(), nota.getEstudianteId())
                .map(notaExistente -> {
                    notaExistente.setValor(nota.getValor());
                    return notaRepository.save(notaExistente);
                })
                .orElseGet(() -> notaRepository.save(nota));
    }

    public List<Nota> guardarBulkYPublicar(List<Nota> notas) {
        if (notas.isEmpty()) {
            return List.of();
        }

        List<Nota> guardadas = notas.stream()
                .map(this::guardarOActualizarNota)
                .collect(Collectors.toList());

        Integer evaluacionId = notas.get(0).getEvaluacionId();
        String evaluacionNombre = evaluacionRepository.findById(evaluacionId)
                .map(e -> e.getNombre())
                .orElse(null);

        List<Integer> estudianteIds = notas.stream()
                .map(Nota::getEstudianteId)
                .collect(Collectors.toList());

        List<NotificacionNotasEvent.Destinatario> destinatarios =
                estudianteRepository.findAllById(estudianteIds).stream()
                        .filter(e -> e.getUsuario() != null)
                        .map(e -> {
                            Usuario u = e.getUsuario();
                            String nombreCompleto = u.getNombre() + " " + u.getApellidoPaterno() + " " + u.getApellidoMaterno();
                            return new NotificacionNotasEvent.Destinatario(u.getEmail(), nombreCompleto);
                        })
                        .collect(Collectors.toList());

        NotificacionNotasEvent event = new NotificacionNotasEvent(evaluacionId, evaluacionNombre, destinatarios);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);

        return guardadas;
    }

    public List<Nota> listarTodas() {
        return notaRepository.findAll();
    }

    public List<NotaEstudianteProjection> obtenerNotasPorCursoYAsignatura(Integer cursoId, Integer asignaturaId) {
        return notaRepository.findNotasByCursoAndAsignatura(cursoId, asignaturaId);
    }
}
