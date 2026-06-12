package com.example.BackGestion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionNotasEvent {

    private Integer evaluacionId;
    private String evaluacionNombre;
    private List<Destinatario> destinatarios;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Destinatario {
        private String email;
        private String nombreCompleto;
    }
}
