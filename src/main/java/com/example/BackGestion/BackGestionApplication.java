package com.example.BackGestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class BackGestionApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackGestionApplication.class, args);
	}

}
