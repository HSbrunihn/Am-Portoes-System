
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

    private ProdutoResponse resposta(Long id, String nome) {
        return new ProdutoResponse(
                id, 12L, nome, new BigDecimal("1200.00"), 4, 2, null
        );
    }

    @Test
    void givenProdutoValidoQuandoCriaEntaoRetorna201() throws Exception {
        when(produtoService.criar(any(ProdutoRequest.class)))
                .thenReturn(resposta(7L, "Portao"));

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
    void givenNomeVazioQuandoCriaEntaoRetorna400() throws Exception {
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"codigo\":12,\"preco\":1200.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists());

        verifyNoInteractions(produtoService);
    }

    @Test
    void givenJsonInvalidoQuandoCriaEntaoRetorna400() throws Exception {
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json-invalido"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(produtoService);
    }

    @Test
    void givenProdutosExistentesQuandoListaEntaoRetornaColecao() throws Exception {
        when(produtoService.listar(null))
                .thenReturn(List.of(
                        resposta(1L, "Motor"),
                        resposta(2L, "Portao")
                ));

        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Motor"))
                .andExpect(jsonPath("$[1].nome").value("Portao"));

        verify(produtoService).listar(null);
    }

    @Test
    void givenProdutoExistenteQuandoBuscaPorIdEntaoRetorna200() throws Exception {
        when(produtoService.buscarPorId(7L))
                .thenReturn(resposta(7L, "Portao"));

        mockMvc.perform(get("/api/produtos/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nome").value("Portao"));

        verify(produtoService).buscarPorId(7L);
    }

    @Test
    void givenProdutoInexistenteQuandoBuscaPorIdEntaoRetorna404() throws Exception {
        when(produtoService.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException("Produto nao foi encontrado"));

        mockMvc.perform(get("/api/produtos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Produto nao foi encontrado"));

        verify(produtoService).buscarPorId(99L);
    }

    @Test
    void givenDadosValidosQuandoAtualizaEntaoRetorna200() throws Exception {
        when(produtoService.atualizar(eq(7L), any(ProdutoRequest.class)))
                .thenReturn(resposta(7L, "Portao atualizado"));

        mockMvc.perform(put("/api/produtos/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Portao atualizado\",\"codigo\":12,\"preco\":1200.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Portao atualizado"));

        verify(produtoService).atualizar(eq(7L), any(ProdutoRequest.class));
    }

    @Test
    void givenNomeVazioQuandoAtualizaEntaoRetorna400() throws Exception {
        mockMvc.perform(put("/api/produtos/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"codigo\":12,\"preco\":1200.00}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(produtoService);
    }

    @Test
    void givenProdutoExistenteQuandoExcluiEntaoRetorna204() throws Exception {
        mockMvc.perform(delete("/api/produtos/7"))
                .andExpect(status().isNoContent());

        verify(produtoService).excluir(7L);
    }

    @Test
    void givenProdutosComBaixoEstoqueQuandoConsultaEntaoRetornaLista() throws Exception {
        when(produtoService.listarProdutosComBaixoEstoqueResponse())
                .thenReturn(List.of(resposta(1L, "Motor")));

        mockMvc.perform(get("/api/produtos/baixo-estoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Motor"));

        verify(produtoService).listarProdutosComBaixoEstoqueResponse();
    }

    @Test
    void givenNenhumProdutoComBaixoEstoqueQuandoConsultaEntaoRetornaListaVazia() throws Exception {
        when(produtoService.listarProdutosComBaixoEstoqueResponse())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/produtos/baixo-estoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(produtoService).listarProdutosComBaixoEstoqueResponse();
    }
}
