package com.example.BackGestion.Model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "curso_asignatura_docente")
public class CAD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "curso_id")
    private Integer cursoId;

    @Column(name = "asignatura_id")
    private Integer asignaturaId;

    @Column(name = "docente_id")
    private Integer docenteId;

    @Column(name = "sala_id")
    private Integer salaId;
}
