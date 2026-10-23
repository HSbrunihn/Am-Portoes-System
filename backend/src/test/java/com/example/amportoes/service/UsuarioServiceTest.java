package com.example.amportoes.service;

import com.example.amportoes.controller.request.UsuarioRequest;
import com.example.amportoes.controller.response.UsuarioResponse;
import com.example.amportoes.entity.Usuario;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UsuarioService usuarioService;

    @Test
    void givenDadosValidosQuandoCriaUsuarioEntaoCodificaSenhaEDevolveDadosPublicos() {
        // Given
        UsuarioRequest request = new UsuarioRequest("Ana", "ana@example.com", "senha123");

        when(passwordEncoder.encode("senha123")).thenReturn("hash-senha");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(call -> {
            Usuario salvo = call.getArgument(0);
            salvo.setId(5L);
            return salvo;
        });

        // When
        UsuarioResponse resultado = usuarioService.criar(request);

        // Then
        assertEquals(5L, resultado.id());
        assertEquals("ana@example.com", resultado.email());
        verify(usuarioRepository).save(argThat(u -> "hash-senha".equals(u.getSenha())));
    }

    @Test
    void givenNomeDeBuscaQuandoListaUsuariosEntaoUsaBuscaParcialIgnorandoCaixa() {
        // Given
        when(usuarioRepository.findByNomeContainingIgnoreCase("ana"))
                .thenReturn(List.of(
                        new Usuario(2L, "Ana Silva", "ana@example.com", "hash")
                ));

        // When
        List<UsuarioResponse> resultado = usuarioService.listar("ana");

        // Then
        assertEquals(1, resultado.size());
        assertEquals("Ana Silva", resultado.get(0).nome());
        verify(usuarioRepository).findByNomeContainingIgnoreCase("ana");
        verify(usuarioRepository, never()).findAll();
    }

    @Test
    void givenNomeNuloQuandoListaUsuariosEntaoRetornaTodos() {
        // Given
        when(usuarioRepository.findAll())
                .thenReturn(List.of(
                        new Usuario(1L, "Ana", "ana@example.com", "hash"),
                        new Usuario(2L, "Bruno", "bruno@example.com", "hash")
                ));

        // When
        List<UsuarioResponse> resultado = usuarioService.listar(null);

        // Then
        assertEquals(2, resultado.size());
        verify(usuarioRepository).findAll();
        verify(usuarioRepository, never()).findByNomeContainingIgnoreCase(anyString());
    }

    @Test
    void givenNomeEmBrancoQuandoListaUsuariosEntaoRetornaTodos() {
        // Given
        when(usuarioRepository.findAll()).thenReturn(List.of());

        // When
        List<UsuarioResponse> resultado = usuarioService.listar("   ");

        // Then
        assertTrue(resultado.isEmpty());
        verify(usuarioRepository).findAll();
        verify(usuarioRepository, never()).findByNomeContainingIgnoreCase(anyString());
    }

    @Test
    void givenUsuarioExistenteQuandoBuscaPorIdEntaoRetornaDadosPublicos() {
        // Given
        Usuario usuario = new Usuario(7L, "Ana", "ana@example.com", "hash");
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));

        // When
        UsuarioResponse resultado = usuarioService.buscarPorId(7L);

        // Then
        assertNotNull(resultado);
        assertEquals(7L, resultado.id());
        assertEquals("Ana", resultado.nome());
        assertEquals("ana@example.com", resultado.email());
        verify(usuarioRepository).findById(7L);
    }

    @Test
    void givenUsuarioInexistenteQuandoBuscaPorIdEntaoLancaRecursoNaoEncontrado() {
        // Given
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> usuarioService.buscarPorId(99L)
        );

        verify(usuarioRepository).findById(99L);
    }

    @Test
    void givenUsuarioExistenteQuandoAtualizaDadosEntaoCodificaNovaSenhaESalva() {
        // Given
        Usuario existente = new Usuario(7L, "Ana", "ana@example.com", "hash-antigo");
        UsuarioRequest request = new UsuarioRequest("Ana Lima", "lima@example.com", "nova123");

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("nova123")).thenReturn("hash-novo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(call -> call.getArgument(0));

        // When
        UsuarioResponse resultado = usuarioService.atualizar(7L, request);

        // Then
        assertEquals("Ana Lima", resultado.nome());
        assertEquals("lima@example.com", resultado.email());
        verify(usuarioRepository).save(argThat(u -> "hash-novo".equals(u.getSenha())));
    }

    @Test
    void givenUsuarioExistenteQuandoDeletaEntaoRemoveUsuario() {
        // Given
        when(usuarioRepository.existsById(20L)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> usuarioService.deletar(20L));

        // Then
        verify(usuarioRepository).existsById(20L);
        verify(usuarioRepository).deleteById(20L);
    }

    @Test
    void givenUsuarioInexistenteQuandoDeletaEntaoLancaExcecaoESemApagar() {
        // Given
        when(usuarioRepository.existsById(20L)).thenReturn(false);

        // When / Then
        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> usuarioService.deletar(20L)
        );

        verify(usuarioRepository).existsById(20L);
        verify(usuarioRepository, never()).deleteById(anyLong());
    }
}