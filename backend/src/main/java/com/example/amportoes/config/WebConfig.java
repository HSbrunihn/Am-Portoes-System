package com.example.amportoes.config;

import com.example.amportoes.entity.Usuario;
import com.example.amportoes.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*").allowCredentials(true);
    }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean CommandLineRunner administradorInicial(UsuarioRepository usuarios, PasswordEncoder encoder) {
        return args -> {
            if (!usuarios.existsByEmailIgnoreCase("admin@amportoes.com.br")) {
                usuarios.save(new Usuario(null, "Administrador", "admin@amportoes.com.br", encoder.encode("Admin@123")));
            }
        };
    }
}
