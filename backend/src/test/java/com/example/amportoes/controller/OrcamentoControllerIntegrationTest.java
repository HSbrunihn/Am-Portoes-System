
        package com.example.amportoes.controller;

import com.example.amportoes.controller.response.OrcamentoResponse;
import com.example.amportoes.entity.StatusTarefa;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.repository.UsuarioRepository;
import com.example.amportoes.service.OrcamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrcamentoController.class)
class OrcamentoControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OrcamentoService orcamentoService;

    @MockitoBean
    UsuarioRepository usuarioRepository;

    private OrcamentoResponse resposta(Long id, String titulo) {
        return new OrcamentoResponse(
                id,
                titulo,
                "Descricao",
                new BigDecimal("1500.00"),
                LocalDate.of(2026, 12, 20),
                StatusTarefa.PENDENTE
        );
    }

    @Test
    void givenOrcamentoValidoQuandoCriaEntaoRetorna201() throws Exception {
        when(orcamentoService.criar(any()))
                .thenReturn(resposta(5L, "Instalacao"));

        mockMvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Instalacao\",\"descricao\":\"Descricao\",\"valor\":1500.00,\"dataVencimento\":\"2026-12-20\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.titulo").value("Instalacao"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        verify(orcamentoService).criar(any());
    }

    @Test
    void givenValorNegativoQuandoCriaEntaoRetorna400() throws Exception {
        String corpo = "{\"titulo\":\"Instalacao\",\"valor\":-1,\"dataVencimento\":\"2026-12-20\"}";

        mockMvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valor").exists());

        verifyNoInteractions(orcamentoService);
    }

    @Test
    void givenJsonInvalidoQuandoCriaEntaoRetorna400() throws Exception {
        mockMvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json-invalido"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orcamentoService);
    }

    @Test
    void givenOrcamentosExistentesQuandoListaSemFiltroEntaoRetornaLista() throws Exception {
        when(orcamentoService.listar(null))
                .thenReturn(List.of(
                        resposta(1L, "Orcamento A"),
                        resposta(2L, "Orcamento B")
                ));

        mockMvc.perform(get("/api/orcamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].titulo").value("Orcamento A"))
                .andExpect(jsonPath("$[1].titulo").value("Orcamento B"));

        verify(orcamentoService).listar(null);
    }

    @Test
    void givenStatusQuandoListaEntaoEncaminhaFiltroAoService() throws Exception {
        when(orcamentoService.listar(StatusTarefa.PENDENTE))
                .thenReturn(List.of(resposta(1L, "Orcamento A")));

        mockMvc.perform(get("/api/orcamentos")
                        .param("status", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(orcamentoService).listar(StatusTarefa.PENDENTE);
    }

    @Test
    void givenOrcamentoExistenteQuandoBuscaPorIdEntaoRetorna200() throws Exception {
        when(orcamentoService.buscarPorId(5L))
                .thenReturn(resposta(5L, "Instalacao"));

        mockMvc.perform(get("/api/orcamentos/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.titulo").value("Instalacao"));

        verify(orcamentoService).buscarPorId(5L);
    }

    @Test
    void givenOrcamentoInexistenteQuandoBuscaPorIdEntaoRetorna404() throws Exception {
        when(orcamentoService.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException("Orcamento nao encontrado"));

        mockMvc.perform(get("/api/orcamentos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Orcamento nao encontrado"));
    }

    @Test
    void givenDadosValidosQuandoAtualizaEntaoRetorna200() throws Exception {
        when(orcamentoService.atualizar(eq(5L), any()))
                .thenReturn(resposta(5L, "Atualizado"));

        mockMvc.perform(put("/api/orcamentos/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Atualizado\",\"descricao\":\"Descricao\",\"valor\":1500.00,\"dataVencimento\":\"2026-12-20\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Atualizado"));

        verify(orcamentoService).atualizar(eq(5L), any());
    }

    @Test
    void givenValorNegativoQuandoAtualizaEntaoRetorna400() throws Exception {
        mockMvc.perform(put("/api/orcamentos/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Atualizado\",\"valor\":-1,\"dataVencimento\":\"2026-12-20\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orcamentoService);
    }

    @Test
    void givenOrcamentoExistenteQuandoDeletaEntaoRetorna204() throws Exception {
        doNothing().when(orcamentoService).deletar(5L);

        mockMvc.perform(delete("/api/orcamentos/5"))
                .andExpect(status().isNoContent());

        verify(orcamentoService).deletar(5L);
    }
}