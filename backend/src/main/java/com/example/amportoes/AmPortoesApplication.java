package com.example.amportoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

// A autenticação é só por JWT (JwtAuthFilter): o usuário em memória padrão do Spring não é usado
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class AmPortoesApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmPortoesApplication.class, args);
	}

}
