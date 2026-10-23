package com.example.amportoes.controller;

import com.example.amportoes.dto.MovimentacaoResponseDTO;
import com.example.amportoes.entity.TipoMovimentacao;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.exception.RegraNegocioException;
import com.example.amportoes.repository.UsuarioRepository;
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

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MovimentacaoService movimentacaoService;

    @MockitoBean
    UsuarioRepository usuarioRepository;

    @Test
    void givenMovimentacaoValidaQuandoRegistraEntaoRetorna201ComDadosDoService() throws Exception {
        when(movimentacaoService.registrarMovimentacao(any()))
                .thenReturn(new MovimentacaoResponseDTO(
                        1L, 7L, "Portao", 2, TipoMovimentacao.ENTRADA,
                        null, LocalDateTime.of(2026, 10, 1, 10, 0)
                ));

        mockMvc.perform(post("/api/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produtoId\":7,\"quantidade\":2,\"tipo\":\"ENTRADA\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoNome").value("Portao"))
                .andExpect(jsonPath("$.tipo").value("ENTRADA"));
    }

    @Test
    void givenQuantidadeZeroQuandoRegistraMovimentacaoEntaoRetorna400ComErroDeValidacao() throws Exception {
        String corpoInvalido =
                "{\"produtoId\":7,\"quantidade\":0,\"tipo\":\"ENTRADA\"}";

        mockMvc.perform(post("/api/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.quantidade").exists());

        verifyNoInteractions(movimentacaoService);
    }

    @Test
    void givenProdutoInexistenteNoServiceQuandoListaMovimentacoesPorProdutoEntaoRetorna404() throws Exception {
        when(movimentacaoService.listarPorProduto(55L))
                .thenThrow(new RecursoNaoEncontradoException("Produto não encontrado"));

        mockMvc.perform(get("/api/movimentacoes/produto/55"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void givenEstoqueInsuficienteNoServiceQuandoRegistraSaidaEntaoRetorna400ComMensagemDaRegra() throws Exception {
        when(movimentacaoService.registrarMovimentacao(any()))
                .thenThrow(new RegraNegocioException("Estoque insuficiente"));

        mockMvc.perform(post("/api/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produtoId\":7,\"quantidade\":20,\"tipo\":\"SAIDA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Estoque insuficiente"));
    }

    @Test
    void givenListaDeMovimentacoesDoServiceQuandoListaEntaoRetornaColecaoJson() throws Exception {
        when(movimentacaoService.listarMovimentacoes())
                .thenReturn(List.of(new MovimentacaoResponseDTO(
                        1L, 7L, "Portao", 2, TipoMovimentacao.ENTRADA,
                        null, LocalDateTime.of(2026, 10, 1, 10, 0)
                )));

        mockMvc.perform(get("/api/movimentacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantidade").value(2));
    }

    @Test
    void givenNenhumaMovimentacaoQuandoListaPorProdutoEntaoRetornaListaVazia() throws Exception {
        when(movimentacaoService.listarPorProduto(7L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/movimentacoes/produto/7"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(movimentacaoService).listarPorProduto(7L);
    }

    @Test
    void givenCorpoJsonVazioQuandoRegistraMovimentacaoEntaoRetorna400() throws Exception {
        mockMvc.perform(post("/api/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(movimentacaoService);
    }
}