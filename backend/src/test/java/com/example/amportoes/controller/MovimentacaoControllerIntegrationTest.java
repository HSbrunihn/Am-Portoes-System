package com.example.amportoes.controller;

import com.example.amportoes.dto.MovimentacaoResponseDTO;
import com.example.amportoes.entity.TipoMovimentacao;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.exception.RegraNegocioException;
import com.example.amportoes.service.MovimentacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovimentacaoController.class)
class MovimentacaoControllerIntegrationTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean MovimentacaoService movimentacaoService;

    @Test
    void givenMovimentacaoValidaQuandoRegistraEntaoRetorna201ComDadosDoService() throws Exception {
        // Given
        when(movimentacaoService.registrarMovimentacao(any())).thenReturn(new MovimentacaoResponseDTO(1L, 7L, "Portao", 2, TipoMovimentacao.ENTRADA, null, LocalDateTime.of(2026, 10, 1, 10, 0)));

        // When
        mockMvc.perform(post("/api/movimentacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produtoId\":7,\"quantidade\":2,\"tipo\":\"ENTRADA\"}"))
                // Then
                .andExpect(status().isCreated()).andExpect(jsonPath("$.produtoNome").value("Portao"))
                .andExpect(jsonPath("$.tipo").value("ENTRADA"));
    }

    @Test
    void givenQuantidadeZeroQuandoRegistraMovimentacaoEntaoRetorna400ComErroDeValidacao() throws Exception {
        // Given
        String corpoInvalido = "{\"produtoId\":7,\"quantidade\":0,\"tipo\":\"ENTRADA\"}";

        // When
        mockMvc.perform(post("/api/movimentacoes").contentType(MediaType.APPLICATION_JSON).content(corpoInvalido))
                // Then
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.quantidade").exists());
        verifyNoInteractions(movimentacaoService);
    }

    @Test
    void givenProdutoInexistenteNoServiceQuandoListaMovimentacoesPorProdutoEntaoRetorna404() throws Exception {
        // Given
        when(movimentacaoService.listarPorProduto(55L)).thenThrow(new RecursoNaoEncontradoException("Produto não encontrado"));

        // When
        mockMvc.perform(get("/api/movimentacoes/produto/55"))
                // Then
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void givenEstoqueInsuficienteNoServiceQuandoRegistraSaidaEntaoRetorna400ComMensagemDaRegra() throws Exception {
        // Given
        when(movimentacaoService.registrarMovimentacao(any())).thenThrow(new RegraNegocioException("Estoque insuficiente"));

        // When
        mockMvc.perform(post("/api/movimentacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produtoId\":7,\"quantidade\":20,\"tipo\":\"SAIDA\"}"))
                // Then
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").value("Estoque insuficiente"));
    }

    @Test
    void givenListaDeMovimentacoesDoServiceQuandoListaEntaoRetornaColecaoJson() throws Exception {
        // Given
        when(movimentacaoService.listarMovimentacoes()).thenReturn(List.of(new MovimentacaoResponseDTO(1L, 7L, "Portao", 2, TipoMovimentacao.ENTRADA, null, LocalDateTime.of(2026, 10, 1, 10, 0))));

        // When
        mockMvc.perform(get("/api/movimentacoes"))
                // Then
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].quantidade").value(2));
    }
}