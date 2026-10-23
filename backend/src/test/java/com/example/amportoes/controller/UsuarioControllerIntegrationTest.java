package com.example.amportoes.controller;

import com.example.amportoes.repository.UsuarioRepository;

import com.example.amportoes.controller.response.UsuarioResponse;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerIntegrationTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean UsuarioService usuarioService;

    @MockitoBean UsuarioRepository usuarioRepository;

    @Test
    void givenUsuarioValidoQuandoCadastraEntaoRetorna201SemExporSenha() throws Exception {
        // Given
        when(usuarioService.criar(any())).thenReturn(new UsuarioResponse(8L, "Ana", "ana@example.com"));

        // When
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@example.com\",\"senha\":\"senha123\"}"))
                // Then
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void givenEmailInvalidoQuandoCadastraUsuarioEntaoRetorna400ComErroDeValidacao() throws Exception {
        // Given
        String corpoInvalido = "{\"nome\":\"Ana\",\"email\":\"email-invalido\",\"senha\":\"senha123\"}";

        // When
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(corpoInvalido))
                // Then
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.email").exists());
        verifyNoInteractions(usuarioService);
    }

    @Test
    void givenUsuarioInexistenteNoServiceQuandoBuscaPorIdEntaoRetorna404() throws Exception {
        // Given
        when(usuarioService.buscarPorId(33L)).thenThrow(new RecursoNaoEncontradoException("Usuario nao encontrado"));

        // When
        mockMvc.perform(get("/api/usuarios/33"))
                // Then
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void givenListaDeUsuariosDoServiceQuandoListaEntaoRetornaUsuariosEmJson() throws Exception {
        // Given
        when(usuarioService.listar(null)).thenReturn(List.of(new UsuarioResponse(1L, "Ana", "ana@example.com")));

        // When
        mockMvc.perform(get("/api/usuarios"))
                // Then
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].email").value("ana@example.com"));
    }
}