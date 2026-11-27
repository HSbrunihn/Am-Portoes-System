package com.example.amportoes.config;

import com.example.amportoes.entity.Usuario;
import com.example.amportoes.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class WebConfig {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner administradorInicial(
            UsuarioRepository usuarios,
            PasswordEncoder encoder,
            @Value("${app.admin.email:admin@amportoes.com.br}") String email,
            @Value("${app.admin.password:}") String senha) {
        return args -> {
            if (senha.isBlank()) {
                log.warn("ADMIN_PASSWORD não definida: administrador inicial não será criado");
                return;
            }
            if (!usuarios.existsByEmailIgnoreCase(email)) {
                usuarios.save(new Usuario(null, "Administrador", email, encoder.encode(senha)));
            }
        };
    }
}