package com.example.BackGestion.Repository;

import com.example.BackGestion.Model.Estudiante;
import com.example.BackGestion.dto.EstudianteCursoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {

    @Query(value = "SELECT " +
                   "    c.nivel || ' ' || c.letra AS cursoNombre, " +
                   "    c.anio_academico AS anioAcademico, " +
                   "    a.nombre AS asignatura, " +
                   "    u.rut, " +
                   "    u.apellido_paterno || ' ' || u.apellido_materno || ', ' || u.nombre AS estudianteFullName, " +
                   "    d_u.nombre || ' ' || d_u.apellido_paterno AS docenteACargo, " +
                   "    e.estudiante_id AS estudianteId, " +
                   "    u.email AS email " +
                   "FROM estudiantes e " +
                   "INNER JOIN usuarios u ON e.estudiante_id = u.usuario_id " +
                   "INNER JOIN cursos c ON e.curso_id = c.curso_id " +
                   "INNER JOIN curso_asignatura_docente cad ON c.curso_id = cad.curso_id " +
                   "INNER JOIN asignaturas a ON cad.asignatura_id = a.asignatura_id " +
                   "INNER JOIN docentes d ON cad.docente_id = d.docente_id " +
                   "INNER JOIN usuarios d_u ON d.docente_id = d_u.usuario_id " +
                   "WHERE c.curso_id = :cursoId " +
                   "ORDER BY asignatura ASC, u.apellido_paterno ASC", nativeQuery = true)
    List<EstudianteCursoProjection> findEstudiantesByCursoId(@Param("cursoId") Integer cursoId);
}
