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

    private static final String EMAIL = "admin@amportoes.com.br";

    private final WebConfig webConfig = new WebConfig();

    @Test
    void deveCriarAdministradorQuandoAindaNaoExiste() throws Exception {
        // Given
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(usuarios.existsByEmailIgnoreCase(EMAIL)).thenReturn(false);
        when(encoder.encode("SenhaDeTeste1")).thenReturn("senha-criptografada");

        CommandLineRunner runner =
                webConfig.administradorInicial(usuarios, encoder, EMAIL, "SenhaDeTeste1");

        // When
        runner.run();

        // Then
        verify(usuarios).save(argThat(usuario ->
                "Administrador".equals(usuario.getNome())
                        && EMAIL.equals(usuario.getEmail())
                        && "senha-criptografada".equals(usuario.getSenha())
        ));
    }

    @Test
    void naoDeveCriarAdministradorQuandoJaExiste() throws Exception {
        // Given
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(usuarios.existsByEmailIgnoreCase(EMAIL)).thenReturn(true);

        CommandLineRunner runner =
                webConfig.administradorInicial(usuarios, encoder, EMAIL, "SenhaDeTeste1");

        // When
        runner.run();

        // Then
        verify(usuarios, never()).save(any(Usuario.class));
        verifyNoInteractions(encoder);
    }

    @Test
    void naoDeveCriarAdministradorQuandoSenhaNaoFoiDefinida() throws Exception {
        // Given
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        CommandLineRunner runner =
                webConfig.administradorInicial(usuarios, encoder, EMAIL, "");

        // When
        runner.run();

        // Then
        verifyNoInteractions(usuarios, encoder);
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