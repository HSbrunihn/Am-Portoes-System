package com.example.amportoes.controller;

import com.example.amportoes.repository.UsuarioRepository;

import com.example.amportoes.controller.response.TarefaResponse;
import com.example.amportoes.entity.StatusTarefa;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.service.TarefaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TarefaController.class)
class TarefaControllerIntegrationTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean TarefaService tarefaService;

    @MockitoBean UsuarioRepository usuarioRepository;

    @Test
    void givenTarefaValidaEserviceRetornaTarefaQuandoCriaEntaoRetorna201ComStatusPendente() throws Exception {
        // Given
        when(tarefaService.criar(any())).thenReturn(new TarefaResponse(3L, "Instalar portao", "", null, LocalDate.of(2026, 12, 1), StatusTarefa.PENDENTE));

        // When
        mockMvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Instalar portao\",\"dataVencimento\":\"2026-12-01\"}"))
                // Then
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void givenTituloAusenteQuandoCriaTarefaEntaoRetorna400ComErroDeValidacao() throws Exception {
        // Given
        String corpoInvalido = "{\"titulo\":\"\",\"dataVencimento\":\"2026-12-01\"}";

        // When
        mockMvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON).content(corpoInvalido))
                // Then
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.titulo").exists());
        verifyNoInteractions(tarefaService);
    }

    @Test
    void givenTarefaNaoEncontradaPeloServiceQuandoBuscaPorIdEntaoRetorna404() throws Exception {
        // Given
        when(tarefaService.buscarPorId(44L)).thenThrow(new RecursoNaoEncontradoException("Tarefa não foi encontrada"));

        // When
        mockMvc.perform(get("/api/tarefas/44"))
                // Then
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void givenTarefasRetornadasPeloServiceQuandoFiltraPorStatusEntaoRetornaListaFiltrada() throws Exception {
        // Given
        when(tarefaService.listar(StatusTarefa.CONCLUIDA)).thenReturn(List.of(new TarefaResponse(2L, "Conferir", "", null, LocalDate.of(2026, 12, 1), StatusTarefa.CONCLUIDA)));

        // When
        mockMvc.perform(get("/api/tarefas").param("status", "CONCLUIDA"))
                // Then
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("CONCLUIDA"));
    }
}