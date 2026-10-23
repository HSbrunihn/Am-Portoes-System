
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

import static org.assertj.core.api.Assertions.assertThat;
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

        assertThat(produtoRepository.count()).isEqualTo(1);

        Produto salvo = produtoRepository.findAll().get(0);

        assertThat(salvo.getNome()).isEqualTo("Portao");
        assertThat(salvo.getCodigo()).isEqualTo(5L);
        assertThat(salvo.getPreco()).isEqualByComparingTo("1200.00");
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
                .andExpect(jsonPath("$.codigo", is(1)))
                .andExpect(jsonPath("$.preco", is(1200.0)));
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

        Produto atualizado = produtoRepository.findById(produto.getId())
                .orElseThrow();

        assertThat(atualizado.getNome()).isEqualTo("Portao novo");
        assertThat(atualizado.getCodigo()).isEqualTo(2L);
        assertThat(atualizado.getPreco()).isEqualByComparingTo("1500.00");
    }

    @Test
    void deveExcluirProduto() throws Exception {
        Produto produto = salvar("Portao", 1, "1200.00");

        mockMvc.perform(delete(URL + "/" + produto.getId()))
                .andExpect(status().isNoContent());

        assertThat(produtoRepository.findById(produto.getId())).isEmpty();
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

        assertThat(produtoRepository.count()).isZero();
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

        assertThat(produtoRepository.count()).isZero();
    }

    @Test
    void deveListarProdutosComEstoqueBaixo() throws Exception {
        Produto baixo = salvar("Estoque baixo", 1, "100.00");
        baixo.setQuantidadeEstoque(1);
        baixo.setEstoqueMinimo(2);
        produtoRepository.save(baixo);

        Produto normal = salvar("Estoque normal", 2, "200.00");
        normal.setQuantidadeEstoque(10);
        normal.setEstoqueMinimo(2);
        produtoRepository.save(normal);

        mockMvc.perform(get(URL + "/baixo-estoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Estoque baixo")));
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
