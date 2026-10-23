
        package com.example.amportoes.controller;

import com.example.amportoes.controller.response.TarefaResponse;
import com.example.amportoes.entity.StatusTarefa;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.repository.UsuarioRepository;
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

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TarefaService tarefaService;

    @MockitoBean
    UsuarioRepository usuarioRepository;

    private TarefaResponse resposta(Long id, String titulo, StatusTarefa status) {
        return new TarefaResponse(
                id, titulo, "Descricao", null,
                LocalDate.of(2026, 12, 1), status
        );
    }

    @Test
    void givenTarefaValidaQuandoCriaEntaoRetorna201() throws Exception {
        when(tarefaService.criar(any()))
                .thenReturn(resposta(3L, "Instalar portao", StatusTarefa.PENDENTE));

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Instalar portao\",\"dataVencimento\":\"2026-12-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        verify(tarefaService).criar(any());
    }

    @Test
    void givenTituloAusenteQuandoCriaTarefaEntaoRetorna400() throws Exception {
        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"dataVencimento\":\"2026-12-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").exists());

        verifyNoInteractions(tarefaService);
    }

    @Test
    void givenJsonInvalidoQuandoCriaTarefaEntaoRetorna400() throws Exception {
        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json-invalido"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tarefaService);
    }

    @Test
    void givenTarefasExistentesQuandoListaSemFiltroEntaoRetornaLista() throws Exception {
        when(tarefaService.listar(null)).thenReturn(List.of(
                resposta(1L, "Tarefa A", StatusTarefa.PENDENTE),
                resposta(2L, "Tarefa B", StatusTarefa.CONCLUIDA)
        ));

        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].titulo").value("Tarefa A"));

        verify(tarefaService).listar(null);
    }

    @Test
    void givenStatusQuandoListaEntaoFiltraResultado() throws Exception {
        when(tarefaService.listar(StatusTarefa.CONCLUIDA))
                .thenReturn(List.of(resposta(2L, "Conferir", StatusTarefa.CONCLUIDA)));

        mockMvc.perform(get("/api/tarefas").param("status", "CONCLUIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("CONCLUIDA"));

        verify(tarefaService).listar(StatusTarefa.CONCLUIDA);
    }

    @Test
    void givenTarefaExistenteQuandoBuscaPorIdEntaoRetorna200() throws Exception {
        when(tarefaService.buscarPorId(3L))
                .thenReturn(resposta(3L, "Instalar portao", StatusTarefa.PENDENTE));

        mockMvc.perform(get("/api/tarefas/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.titulo").value("Instalar portao"));

        verify(tarefaService).buscarPorId(3L);
    }

    @Test
    void givenTarefaInexistenteQuandoBuscaPorIdEntaoRetorna404() throws Exception {
        when(tarefaService.buscarPorId(44L))
                .thenThrow(new RecursoNaoEncontradoException("Tarefa nao foi encontrada"));

        mockMvc.perform(get("/api/tarefas/44"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void givenDadosValidosQuandoAtualizaTarefaEntaoRetorna200() throws Exception {
        when(tarefaService.atualizar(eq(3L), any()))
                .thenReturn(resposta(3L, "Tarefa atualizada", StatusTarefa.PENDENTE));

        mockMvc.perform(put("/api/tarefas/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Tarefa atualizada\",\"dataVencimento\":\"2026-12-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Tarefa atualizada"));

        verify(tarefaService).atualizar(eq(3L), any());
    }

    @Test
    void givenTituloInvalidoQuandoAtualizaTarefaEntaoRetorna400() throws Exception {
        mockMvc.perform(put("/api/tarefas/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"dataVencimento\":\"2026-12-01\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tarefaService);
    }

    @Test
    void givenStatusValidoQuandoAtualizaStatusEntaoRetorna200() throws Exception {
        when(tarefaService.atualizarStatus(3L, StatusTarefa.CONCLUIDA))
                .thenReturn(resposta(3L, "Instalar portao", StatusTarefa.CONCLUIDA));

        mockMvc.perform(patch("/api/tarefas/3/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"CONCLUIDA\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));

        verify(tarefaService).atualizarStatus(3L, StatusTarefa.CONCLUIDA);
    }

    @Test
    void givenTarefaExistenteQuandoDeletaEntaoRetorna204() throws Exception {
        mockMvc.perform(delete("/api/tarefas/3"))
                .andExpect(status().isNoContent());

        verify(tarefaService).deletar(3L);
    }
}
