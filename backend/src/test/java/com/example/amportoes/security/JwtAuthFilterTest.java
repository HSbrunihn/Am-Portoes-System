package com.example.amportoes.security;

import com.example.amportoes.entity.Usuario;
import com.example.amportoes.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final JwtAuthFilter filtro = new JwtAuthFilter(jwtService, usuarios);

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private MockFilterChain executar(String authorization) throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        if (authorization != null) {
            requisicao.addHeader("Authorization", authorization);
        }
        MockFilterChain cadeia = new MockFilterChain();
        filtro.doFilter(requisicao, new MockHttpServletResponse(), cadeia);
        return cadeia;
    }

    @Test
    void givenTokenValidoEUsuarioExistenteQuandoFiltraEntaoAutenticaERepassa() throws Exception {
        // Given
        when(jwtService.tokenValido("token-bom")).thenReturn(true);
        when(jwtService.extrairEmail("token-bom")).thenReturn("bruno@example.com");
        when(usuarios.findByEmailIgnoreCase("bruno@example.com"))
                .thenReturn(Optional.of(new Usuario(1L, "Bruno", "bruno@example.com", "hash")));

        // When
        MockFilterChain cadeia = executar("Bearer token-bom");

        // Then
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(autenticacao);
        assertEquals("bruno@example.com", autenticacao.getName());
        assertNotNull(cadeia.getRequest(), "a requisição deve seguir pela cadeia");
    }

    @Test
    void givenSemCabecalhoAuthorizationQuandoFiltraEntaoNaoAutenticaERepassa() throws Exception {
        // When
        MockFilterChain cadeia = executar(null);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(cadeia.getRequest());
        verifyNoInteractions(jwtService, usuarios);
    }

    @Test
    void givenCabecalhoSemPrefixoBearerQuandoFiltraEntaoNaoAutentica() throws Exception {
        // When
        executar("Basic YWRtaW46c2VuaGE=");

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, usuarios);
    }

    @Test
    void givenTokenInvalidoQuandoFiltraEntaoNaoAutentica() throws Exception {
        // Given
        when(jwtService.tokenValido("token-ruim")).thenReturn(false);

        // When
        MockFilterChain cadeia = executar("Bearer token-ruim");

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(cadeia.getRequest());
        verify(jwtService, never()).extrairEmail(anyString());
        verifyNoInteractions(usuarios);
    }

    @Test
    void givenTokenValidoMasUsuarioApagadoQuandoFiltraEntaoNaoAutentica() throws Exception {
        // Given
        when(jwtService.tokenValido("token-bom")).thenReturn(true);
        when(jwtService.extrairEmail("token-bom")).thenReturn("fantasma@example.com");
        when(usuarios.findByEmailIgnoreCase("fantasma@example.com")).thenReturn(Optional.empty());

        // When
        executar("Bearer token-bom");

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void givenJaExisteAutenticacaoQuandoFiltraEntaoNaoSobrescreve() throws Exception {
        // Given
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("outro", null));
        when(jwtService.tokenValido("token-bom")).thenReturn(true);

        // When
        executar("Bearer token-bom");

        // Then
        assertEquals("outro", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(jwtService, never()).extrairEmail(anyString());
    }
}