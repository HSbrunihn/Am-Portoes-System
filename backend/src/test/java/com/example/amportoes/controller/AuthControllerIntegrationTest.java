package com.example.amportoes.controller;

import com.example.amportoes.controller.request.LoginRequest;
import com.example.amportoes.entity.Usuario;
import com.example.amportoes.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerIntegrationTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean UsuarioRepository usuarioRepository;
    @MockitoBean PasswordEncoder passwordEncoder;

    @Test
    void givenCredenciaisValidasQuandoFazLoginEntaoRetornaUsuarioSemSenhaECriaSessao() throws Exception {
        // Given
        Usuario usuario = new Usuario(2L, "Bruno", "bruno@example.com", "hash");
        when(usuarioRepository.findByEmailIgnoreCase("bruno@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "hash")).thenReturn(true);

        // When
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bruno@example.com\",\"senha\":\"senha123\"}"))
                // Then
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Bruno"))
                .andExpect(jsonPath("$.senha").doesNotExist()).andExpect(request().sessionAttribute("usuarioId", 2L));
    }

    @Test
    void givenSenhaIncorretaQuandoFazLoginEntaoRetorna401() throws Exception {
        // Given
        when(usuarioRepository.findByEmailIgnoreCase("bruno@example.com"))
                .thenReturn(Optional.of(new Usuario(2L, "Bruno", "bruno@example.com", "hash")));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        // When
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bruno@example.com\",\"senha\":\"errada\"}"))
                // Then
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenEmailInvalidoQuandoFazLoginEntaoRetorna400ComErroDeValidacao() throws Exception {
        // Given
        String corpoInvalido = "{\"email\":\"invalido\",\"senha\":\"senha123\"}";

        // When
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpoInvalido))
                // Then
                .andExpect(status().isBadRequest());
        verifyNoInteractions(usuarioRepository, passwordEncoder);
    }

    @Test
    void givenSessaoSemUsuarioAutenticadoQuandoConsultaMeEntaoRetorna401() throws Exception {
        // Given / When
        mockMvc.perform(get("/api/auth/me"))
                // Then
                .andExpect(status().isUnauthorized());
    }
}