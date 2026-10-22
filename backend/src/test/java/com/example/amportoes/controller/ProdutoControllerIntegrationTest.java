
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
class ProdutoControllerIntegrationTest {

    private static final String URL = "/api/produtos";

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
    void deveCriarProduto() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Portao", 5, "1200.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Portao")))
                .andExpect(jsonPath("$.codigo", is(5)))
                .andExpect(jsonPath("$.preco", is(1200.0)));

        org.assertj.core.api.Assertions.assertThat(produtoRepository.count())
                .isEqualTo(1);
    }

    @Test
    void deveListarProdutos() throws Exception {
        salvar("Portao", 1, "1200.00");
        salvar("Porta", 2, "800.00");

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nome",
                        containsInAnyOrder("Portao", "Porta")));
    }

    @Test
    void deveBuscarProdutoPorId() throws Exception {
        Produto produto = salvar("Portao", 1, "1200.00");

        mockMvc.perform(get(URL + "/" + produto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Portao")))
                .andExpect(jsonPath("$.codigo", is(1)));
    }

    @Test
    void deveAtualizarProduto() throws Exception {
        Produto produto = salvar("Portao", 1, "1200.00");

        mockMvc.perform(put(URL + "/" + produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Portao novo", 2, "1500.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Portao novo")))
                .andExpect(jsonPath("$.codigo", is(2)))
                .andExpect(jsonPath("$.preco", is(1500.0)));
    }

    @Test
    void deveExcluirProduto() throws Exception {
        Produto produto = salvar("Portao", 1, "1200.00");

        mockMvc.perform(delete(URL + "/" + produto.getId()))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(
                produtoRepository.findById(produto.getId())
        ).isEmpty();
    }

    @Test
    void deveRetornar404ParaProdutoInexistente() throws Exception {
        mockMvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void deveRetornar400ParaProdutoInvalido() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "",
                                  "preco": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.codigo").exists())
                .andExpect(jsonPath("$.campos.preco").exists());
    }

    @Test
    void deveRetornar400ParaCodigoAusente() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Produto",
                                  "preco": 100.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.codigo").exists());
    }

    @Test
    void deveListarProdutosComEstoqueBaixo() throws Exception {
        salvar("Estoque baixo", 1, "100.00");
        salvar("Estoque normal", 2, "200.00");

        mockMvc.perform(get(URL + "/baixo-estoque"))
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirCorsParaFrontend() throws Exception {
        mockMvc.perform(options("/api/tarefas")
                        .header("Origin", "http://localhost:4000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "http://localhost:4000"));
    }

    private Produto salvar(String nome, long codigo, String preco) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setCodigo(codigo);
        produto.setDescricao("Produto de teste");
        produto.setPreco(new BigDecimal(preco));
        produto.setQuantidadeEstoque(1);
        produto.setEstoqueMinimo(2);

        return produtoRepository.save(produto);
    }

    private String json(String nome, long codigo, String preco)
            throws Exception {
        return mapper.writeValueAsString(new ProdutoRequestTeste(
                nome, codigo, new BigDecimal(preco)
        ));
    }

    private record ProdutoRequestTeste(
            String nome,
            Long codigo,
            BigDecimal preco
    ) {}
}
