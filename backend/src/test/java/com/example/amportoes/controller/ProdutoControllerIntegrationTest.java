
package com.example.amportoes.controller;

import com.example.amportoes.controller.request.ProdutoRequest;
import com.example.amportoes.controller.response.ProdutoResponse;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.repository.UsuarioRepository;
import com.example.amportoes.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdutoController.class)
class ProdutoControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProdutoService produtoService;

    @MockitoBean
    UsuarioRepository usuarioRepository;

    @Test
    void givenProdutoValidoEInclusaoPeloServiceQuandoCriaEntaoRetornaCriadoComDadosDoProduto() throws Exception {
        // Given
        when(produtoService.criar(any(ProdutoRequest.class)))
                .thenReturn(new ProdutoResponse(
                        7L, 12L, "Portao", new BigDecimal("1200.00"), 4, 2, null
                ));

        // When / Then
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Portao\",\"codigo\":12,\"preco\":1200.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nome").value("Portao"))
                .andExpect(jsonPath("$.preco").value(1200.0));

        verify(produtoService).criar(any(ProdutoRequest.class));
    }

    @Test
    void givenProdutoInexistenteNoServiceQuandoBuscaPorIdEntaoRetorna404ComMensagem() throws Exception {
        // Given
        when(produtoService.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException("Produto não foi encontrado"));

        // When / Then
        mockMvc.perform(get("/api/produtos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Produto não foi encontrado"));

        verify(produtoService).buscarPorId(99L);
    }

    @Test
    void givenNomeObrigatorioAusenteQuandoCriaProdutoEntaoRetorna400ComErroDeValidacao() throws Exception {
        // Given
        String corpoInvalido = "{\"nome\":\"\",\"codigo\":12,\"preco\":1200.00}";

        // When / Then
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists());

        verifyNoInteractions(produtoService);
    }

    @Test
    void givenProdutosDevolvidosPeloServiceQuandoListaEntaoRetornaColecaoComCorpoEsperado() throws Exception {
        // Given
        when(produtoService.listar(null))
                .thenReturn(List.of(
                        new ProdutoResponse(1L, 10L, "Motor", BigDecimal.TEN, 3, 1, null)
                ));

        // When / Then
        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Motor"));

        verify(produtoService).listar(null);
    }
}
