package com.example.amportoes.repository;

import com.example.amportoes.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void givenUsuariosComNomesVariandoMaiusculasQuandoBuscaPorParteDoNomeEntaoRetornaCorrespondenciasSemDiferenciarCaixa() {
        // Given
        usuarioRepository.save(new Usuario(null, "Ana Silva", "ana@example.com", "senha"));
        usuarioRepository.save(new Usuario(null, "ANA Costa", "costa@example.com", "senha"));
        usuarioRepository.save(new Usuario(null, "Bruno Lima", "bruno@example.com", "senha"));

        // When
        List<Usuario> resultado = usuarioRepository.findByNomeContainingIgnoreCase("aNa");

        // Then
        assertThat(resultado).extracting(Usuario::getEmail)
                .containsExactlyInAnyOrder("ana@example.com", "costa@example.com");
    }

    @Test
    void givenUsuarioPersistidoQuandoBuscaEmailIgnorandoCaixaEntaoRetornaUsuarioCorrespondente() {
        // Given
        Usuario usuario = usuarioRepository.save(
                new Usuario(null, "Ana Silva", "Ana@Example.com", "senha")
        );

        // When
        var resultado = usuarioRepository.findByEmailIgnoreCase("ana@example.COM");

        // Then
        assertThat(resultado).contains(usuario);
    }

    @Test
    void givenEmailCadastradoQuandoVerificaExistenciaIgnorandoCaixaEntaoRetornaVerdadeiro() {
        // Given
        usuarioRepository.save(new Usuario(null, "Ana Silva", "ana@example.com", "senha"));

        // When
        boolean existe = usuarioRepository.existsByEmailIgnoreCase("ANA@EXAMPLE.COM");

        // Then
        assertThat(existe).isTrue();
    }
}
