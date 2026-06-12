package com.example.BackGestion.Repository;

import com.example.BackGestion.Model.Docente;
import com.example.BackGestion.dto.DocenteCursoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Integer> {

    @Query(value = "WITH docente_info AS ( " +
                   "    SELECT u.usuario_id, u.nombre, u.apellido_paterno, u.apellido_materno, d.docente_id " +
                   "    FROM usuarios u " +
                   "    INNER JOIN docentes d ON u.usuario_id = d.docente_id " +
                   "    AND u.usuario_id = :docenteId " +
                   "), " +
                   "cursos_asignados AS ( " +
                   "    SELECT cad.id AS cad_id, cad.docente_id, c.nivel, c.letra, c.anio_academico, a.nombre AS asignatura_nombre, c.curso_id, cad.asignatura_id, s.nombre AS sala_nombre " +
                   "    FROM curso_asignatura_docente cad " +
                   "    INNER JOIN cursos c ON cad.curso_id = c.curso_id " +
                   "    INNER JOIN asignaturas a ON cad.asignatura_id = a.asignatura_id " +
                   "    LEFT JOIN salas s ON cad.sala_id = s.sala_id " +
                   ") " +
                   "SELECT di.nombre || ' ' || COALESCE(di.apellido_paterno, '') AS docente, " +
                   "       ca.asignatura_nombre AS asignaturaNombre, " +
                   "       ca.nivel || ' ' || ca.letra AS curso, " +
                   "       ca.anio_academico AS anioAcademico, " +
                   "       ca.curso_id AS cursoId, " +
                   "       ca.asignatura_id AS asignaturaId, " +
                   "       ca.cad_id AS cadId, " +
                   "       ca.sala_nombre AS salaNombre " +
                   "FROM docente_info di " +
                   "INNER JOIN cursos_asignados ca ON di.docente_id = ca.docente_id " +
                   "ORDER BY docente ASC, ca.anio_academico DESC", nativeQuery = true)
    List<DocenteCursoProjection> findCursosByDocenteId(@Param("docenteId") Integer docenteId);

    @Query(value = "SELECT u.nombre || ' ' || u.apellido_paterno as docente, " +
                   "       a.nombre as asignaturaNombre, " +
                   "       c.nivel || ' ' || c.letra as curso, " +
                   "       c.anio_academico as anioAcademico, " +
                   "       c.curso_id as cursoId, " +
                   "       a.asignatura_id as asignaturaId, " +
                   "       cad.id as cadId, " +
                   "       s.nombre as salaNombre " +
                   "FROM curso_asignatura_docente cad " +
                   "JOIN usuarios u ON cad.docente_id = u.usuario_id " +
                   "JOIN cursos c ON cad.curso_id = c.curso_id " +
                   "JOIN asignaturas a ON cad.asignatura_id = a.asignatura_id " +
                   "LEFT JOIN salas s ON cad.sala_id = s.sala_id", nativeQuery = true)
    List<DocenteCursoProjection> findAllCADs();
}
