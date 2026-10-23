package com.example.amportoes.controller;

import com.example.amportoes.controller.response.OrcamentoResponse;
import com.example.amportoes.entity.StatusTarefa;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.service.OrcamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrcamentoController.class)
class OrcamentoControllerIntegrationTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean OrcamentoService orcamentoService;

    @Test
    void givenOrcamentoValidoQuandoCriaEntaoRetorna201ComRespostaDoService() throws Exception {
        // Given
        when(orcamentoService.criar(any())).thenReturn(new OrcamentoResponse(5L, "Instalacao", "", new BigDecimal("1500.00"), LocalDate.of(2026, 12, 20), StatusTarefa.PENDENTE));

        // When
        mockMvc.perform(post("/api/orcamentos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Instalacao\",\"descricao\":\"\",\"valor\":1500.00,\"dataVencimento\":\"2026-12-20\"}"))
                // Then
                .andExpect(status().isCreated()).andExpect(jsonPath("$.titulo").value("Instalacao"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void givenValorNegativoQuandoCriaOrcamentoEntaoRetorna400ComCampoInvalido() throws Exception {
        // Given
        String corpoInvalido = "{\"titulo\":\"Instalacao\",\"valor\":-1,\"dataVencimento\":\"2026-12-20\"}";

        // When
        mockMvc.perform(post("/api/orcamentos").contentType(MediaType.APPLICATION_JSON).content(corpoInvalido))
                // Then
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.valor").exists());
        verifyNoInteractions(orcamentoService);
    }

    @Test
    void givenOrcamentoInexistenteNoServiceQuandoBuscaPorIdEntaoRetorna404ComMensagem() throws Exception {
        // Given
        when(orcamentoService.buscarPorId(99L)).thenThrow(new RecursoNaoEncontradoException("Orcamento não encontrado"));

        // When
        mockMvc.perform(get("/api/orcamentos/99"))
                // Then
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.mensagem").value("Orcamento não encontrado"));
    }
}