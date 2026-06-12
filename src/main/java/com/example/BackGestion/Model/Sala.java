package com.example.BackGestion.Model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "salas")
public class Sala {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sala_id")
    private Integer salaId;

    private String nombre;
}
