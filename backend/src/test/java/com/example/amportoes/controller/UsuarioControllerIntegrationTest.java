
        package com.example.amportoes.controller;

import com.example.amportoes.controller.request.UsuarioRequest;
import com.example.amportoes.controller.response.UsuarioResponse;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.repository.UsuarioRepository;
import com.example.amportoes.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UsuarioService usuarioService;

    @MockitoBean
    UsuarioRepository usuarioRepository;

    @Test
    void givenUsuarioValidoQuandoCadastraEntaoRetorna201SemExporSenha() throws Exception {
        when(usuarioService.criar(any(UsuarioRequest.class)))
                .thenReturn(new UsuarioResponse(8L, "Ana", "ana@example.com"));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@example.com\",\"senha\":\"senha123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        verify(usuarioService).criar(any(UsuarioRequest.class));
    }

    @Test
    void givenEmailInvalidoQuandoCadastraEntaoRetorna400() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"email-invalido\",\"senha\":\"senha123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void givenJsonInvalidoQuandoCadastraEntaoRetorna400() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json-invalido"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void givenUsuariosQuandoListaSemFiltroEntaoRetornaLista() throws Exception {
        when(usuarioService.listar(null))
                .thenReturn(List.of(
                        new UsuarioResponse(1L, "Ana", "ana@example.com"),
                        new UsuarioResponse(2L, "Carlos", "carlos@example.com")
                ));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Ana"))
                .andExpect(jsonPath("$[1].nome").value("Carlos"));

        verify(usuarioService).listar(null);
    }

    @Test
    void givenNomeQuandoListaComFiltroEntaoEnviaNomeAoService() throws Exception {
        when(usuarioService.listar("Ana"))
                .thenReturn(List.of(new UsuarioResponse(1L, "Ana", "ana@example.com")));

        mockMvc.perform(get("/api/usuarios").param("nome", "Ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Ana"));

        verify(usuarioService).listar("Ana");
    }

    @Test
    void givenUsuarioExistenteQuandoBuscaPorIdEntaoRetorna200() throws Exception {
        when(usuarioService.buscarPorId(8L))
                .thenReturn(new UsuarioResponse(8L, "Ana", "ana@example.com"));

        mockMvc.perform(get("/api/usuarios/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@example.com"));

        verify(usuarioService).buscarPorId(8L);
    }

    @Test
    void givenUsuarioInexistenteQuandoBuscaPorIdEntaoRetorna404() throws Exception {
        when(usuarioService.buscarPorId(33L))
                .thenThrow(new RecursoNaoEncontradoException("Usuario nao encontrado"));

        mockMvc.perform(get("/api/usuarios/33"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(usuarioService).buscarPorId(33L);
    }

    @Test
    void givenDadosValidosQuandoAtualizaEntaoRetorna200() throws Exception {
        when(usuarioService.atualizar(eq(8L), any(UsuarioRequest.class)))
                .thenReturn(new UsuarioResponse(8L, "Ana Atualizada", "ana@example.com"));

        mockMvc.perform(put("/api/usuarios/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana Atualizada\",\"email\":\"ana@example.com\",\"senha\":\"senha123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Atualizada"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        verify(usuarioService).atualizar(eq(8L), any(UsuarioRequest.class));
    }

    @Test
    void givenEmailInvalidoQuandoAtualizaEntaoRetorna400() throws Exception {
        mockMvc.perform(put("/api/usuarios/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"invalido\",\"senha\":\"senha123\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void givenUsuarioExistenteQuandoDeletaEntaoRetorna204() throws Exception {
        mockMvc.perform(delete("/api/usuarios/8"))
                .andExpect(status().isNoContent());

        verify(usuarioService).deletar(8L);
    }
}
