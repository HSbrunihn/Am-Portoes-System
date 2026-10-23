package com.example.amportoes.config;

import com.example.amportoes.entity.Usuario;
import com.example.amportoes.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebConfigTest {

    private final WebConfig webConfig = new WebConfig();

    @Test
    void deveCriarAdministradorQuandoAindaNaoExiste() throws Exception {
        // Given
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(usuarios.existsByEmailIgnoreCase("admin@amportoes.com.br"))
                .thenReturn(false);
        when(encoder.encode("Admin@123")).thenReturn("senha-criptografada");

        CommandLineRunner runner = webConfig.administradorInicial(usuarios, encoder);

        // When
        runner.run();

        // Then
        verify(usuarios).save(argThat(usuario ->
                "Administrador".equals(usuario.getNome())
                        && "admin@amportoes.com.br".equals(usuario.getEmail())
                        && "senha-criptografada".equals(usuario.getSenha())
        ));
        verify(encoder).encode("Admin@123");
    }

    @Test
    void naoDeveCriarAdministradorQuandoJaExiste() throws Exception {
        // Given
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(usuarios.existsByEmailIgnoreCase("admin@amportoes.com.br"))
                .thenReturn(true);

        CommandLineRunner runner = webConfig.administradorInicial(usuarios, encoder);

        // When
        runner.run();

        // Then
        verify(usuarios, never()).save(any(Usuario.class));
        verifyNoInteractions(encoder);
    }

    @Test
    void deveCriarPasswordEncoder() {
        // When
        PasswordEncoder encoder = webConfig.passwordEncoder();

        // Then
        assertNotNull(encoder);
        assertTrue(encoder.matches("senha123", encoder.encode("senha123")));
    }
}