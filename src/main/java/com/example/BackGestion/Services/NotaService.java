package com.example.BackGestion.Services;

import com.example.BackGestion.Model.Nota;
import com.example.BackGestion.Model.Usuario;
import com.example.BackGestion.Repository.EstudianteRepository;
import com.example.BackGestion.Repository.EvaluacionRepository;
import com.example.BackGestion.Repository.NotaRepository;
import com.example.BackGestion.dto.NotaEstudianteProjection;
import com.example.BackGestion.dto.NotificacionNotasEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotaService {

    private static final Logger log = LoggerFactory.getLogger(NotaService.class);

    @Autowired
    private NotaRepository notaRepository;

    @Autowired
    private EvaluacionRepository evaluacionRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Autowired(required = false)
    private SqsClient sqsClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange:}")
    private String exchange;

    @Value("${rabbitmq.routing-key:}")
    private String routingKey;

    @Value("${app.environment:dev}")
    private String environment;

    @Value("${aws.sqs.queue-url:}")
    private String sqsQueueUrl;

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

        log.info("[RabbitMQ] Guardando {} notas en bulk", notas.size());

        List<Nota> guardadas = notas.stream()
                .map(this::guardarOActualizarNota)
                .collect(Collectors.toList());

        log.info("[RabbitMQ] {} notas guardadas en BD", guardadas.size());

        Integer evaluacionId = notas.get(0).getEvaluacionId();
        String evaluacionNombre = evaluacionRepository.findById(evaluacionId)
                .map(e -> e.getNombre())
                .orElse(null);

        log.info("[RabbitMQ] Evaluacion resuelta: id={}, nombre={}", evaluacionId, evaluacionNombre);

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

        log.info("[RabbitMQ] Destinatarios resueltos: {}", destinatarios.stream()
                .map(NotificacionNotasEvent.Destinatario::getEmail)
                .collect(Collectors.joining(", ")));

        NotificacionNotasEvent event = new NotificacionNotasEvent(evaluacionId, evaluacionNombre, destinatarios);

        if ("prod".equals(environment)) {
            try {
                String json = objectMapper.writeValueAsString(event);
                sqsClient.sendMessage(r -> r.queueUrl(sqsQueueUrl).messageBody(json));
                log.info("[SQS] Evento publicado en cola {}", sqsQueueUrl);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializando evento para SQS", e);
            }
        } else {
            log.info("[RabbitMQ] Publicando evento en exchange='{}' routingKey='{}'", exchange, routingKey);
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("[RabbitMQ] Evento publicado exitosamente");
        }

        return guardadas;
    }

    public List<Nota> listarTodas() {
        return notaRepository.findAll();
    }

    public List<NotaEstudianteProjection> obtenerNotasPorCursoYAsignatura(Integer cursoId, Integer asignaturaId) {
        return notaRepository.findNotasByCursoAndAsignatura(cursoId, asignaturaId);
    }
}
