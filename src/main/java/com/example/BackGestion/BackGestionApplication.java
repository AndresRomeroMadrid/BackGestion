package com.example.BackGestion;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class BackGestionApplication {

	public static void main(String[] args) {
		// Carga el archivo .env (si existe) como variables de entorno del proceso,
		// para que ${JWT_SECRET:default-secret} y demas placeholders puedan resolverlo.
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(entry -> {
			if (System.getProperty(entry.getKey()) == null) {
				System.setProperty(entry.getKey(), entry.getValue());
			}
		});

		SpringApplication.run(BackGestionApplication.class, args);
	}

}
