package com.example.BackGestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackGestionApplication {

	public static void main(String[] args) {
		// NOTA PARA PRUEBAS: 
		// Se han implementado pruebas unitarias en src/test/java.
		// Para ejecutar todas las pruebas: ./mvnw test
		// Pruebas relevantes: UsuarioServiceTest (Mockito) y EstudianteControllerTest (MockMvc).
		SpringApplication.run(BackGestionApplication.class, args);
	}

}
