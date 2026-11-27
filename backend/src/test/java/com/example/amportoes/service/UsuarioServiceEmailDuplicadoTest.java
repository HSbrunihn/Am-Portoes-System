package com.example.amportoes.service;

import com.example.amportoes.controller.request.UsuarioRequest;
import com.example.amportoes.entity.Usuario;
import com.example.amportoes.exception.ConflitoException;
import com.example.amportoes.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceEmailDuplicadoTest {

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final UsuarioService service = new UsuarioService(usuarios, encoder);

    private final UsuarioRequest request =
            new UsuarioRequest("Ana", "ana@example.com", "senha123");

    @Test
    void givenEmailJaCadastradoQuandoCriaEntaoLancaConflitoESemSalvar() {
        // Given
        when(usuarios.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);

        // When / Then
        assertThrows(ConflitoException.class, () -> service.criar(request));
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void givenEmailDeOutroUsuarioQuandoAtualizaEntaoLancaConflitoESemSalvar() {
        // Given
        when(usuarios.findById(1L))
                .thenReturn(Optional.of(new Usuario(1L, "Ana", "antigo@example.com", "hash")));
        when(usuarios.findByEmailIgnoreCase("ana@example.com"))
                .thenReturn(Optional.of(new Usuario(2L, "Outra", "ana@example.com", "hash")));

        // When / Then
        assertThrows(ConflitoException.class, () -> service.atualizar(1L, request));
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void givenMesmoEmailDoProprioUsuarioQuandoAtualizaEntaoPermite() {
        // Given
        Usuario existente = new Usuario(1L, "Ana", "ana@example.com", "hash");
        when(usuarios.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarios.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(existente));
        when(encoder.encode("senha123")).thenReturn("novo-hash");
        when(usuarios.save(any(Usuario.class))).thenAnswer(call -> call.getArgument(0));

        // When
        var resposta = service.atualizar(1L, request);

        // Then
        assertEquals("ana@example.com", resposta.email());
        verify(usuarios).save(existente);
    }
}