
package com.example.amportoes.controller;

import com.example.amportoes.entity.Produto;
import com.example.amportoes.repository.MovimentacaoRepository;
import com.example.amportoes.repository.ProdutoRepository;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MovimentacaoControllerIntegrationTest {

    private static final String URL = "/api/movimentacoes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @BeforeEach
    void limparBanco() {
        movimentacaoRepository.deleteAll();
        produtoRepository.deleteAll();
    }

    @Test
    void deveRegistrarEntradaEAtualizarEstoque() throws Exception {
        Produto produto = salvarProduto(5);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": %d,
                                  "quantidade": 3,
                                  "tipo": "ENTRADA",
                                  "observacao": "Reposição"
                                }
                                """.formatted(produto.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantidade", is(3)))
                .andExpect(jsonPath("$.tipo", is("ENTRADA")));

        Produto atualizado = produtoRepository
                .findById(produto.getId()).orElseThrow();

        org.assertj.core.api.Assertions.assertThat(
                atualizado.getQuantidadeEstoque()).isEqualTo(8);
    }

    @Test
    void deveRegistrarSaidaEAtualizarEstoque() throws Exception {
        Produto produto = salvarProduto(5);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": %d,
                                  "quantidade": 2,
                                  "tipo": "SAIDA"
                                }
                                """.formatted(produto.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantidade", is(2)))
                .andExpect(jsonPath("$.tipo", is("SAIDA")));

        Produto atualizado = produtoRepository
                .findById(produto.getId()).orElseThrow();

        org.assertj.core.api.Assertions.assertThat(
                atualizado.getQuantidadeEstoque()).isEqualTo(3);
    }

    @Test
    void deveRejeitarSaidaMaiorQueEstoque() throws Exception {
        Produto produto = salvarProduto(2);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": %d,
                                  "quantidade": 5,
                                  "tipo": "SAIDA"
                                }
                                """.formatted(produto.getId())))
                .andExpect(status().isBadRequest());

        org.assertj.core.api.Assertions.assertThat(
                movimentacaoRepository.count()).isZero();

        org.assertj.core.api.Assertions.assertThat(
                        produtoRepository.findById(produto.getId())
                                .orElseThrow().getQuantidadeEstoque())
                .isEqualTo(2);
    }

    @Test
    void deveRetornar404ParaProdutoInexistente() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": 999999,
                                  "quantidade": 1,
                                  "tipo": "ENTRADA"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarMovimentacoes() throws Exception {
        Produto produto = salvarProduto(5);

        registrar(produto.getId(), 2, "ENTRADA");

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo", is("ENTRADA")));
    }

    @Test
    void deveListarMovimentacoesPorProduto() throws Exception {
        Produto produto = salvarProduto(5);

        registrar(produto.getId(), 2, "ENTRADA");

        mockMvc.perform(get(URL + "/produto/" + produto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].produtoId",
                        is(produto.getId().intValue())));
    }

    @Test
    void deveRetornar404AoListarProdutoInexistente() throws Exception {
        mockMvc.perform(get(URL + "/produto/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarQuantidadeZero() throws Exception {
        Produto produto = salvarProduto(5);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": %d,
                                  "quantidade": 0,
                                  "tipo": "ENTRADA"
                                }
                                """.formatted(produto.getId())))
                .andExpect(status().isBadRequest());
    }

    private Produto salvarProduto(int estoque) {
        Produto produto = new Produto();
        produto.setNome("Produto de teste");
        produto.setCodigo(100L + estoque);
        produto.setDescricao("Teste de movimentação");
        produto.setPreco(new BigDecimal("100.00"));
        produto.setQuantidadeEstoque(estoque);
        produto.setEstoqueMinimo(2);

        return produtoRepository.save(produto);
    }

    private void registrar(Long produtoId, int quantidade, String tipo)
            throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": %d,
                                  "quantidade": %d,
                                  "tipo": "%s"
                                }
                                """.formatted(produtoId, quantidade, tipo)))
                .andExpect(status().isCreated());
    }
}
