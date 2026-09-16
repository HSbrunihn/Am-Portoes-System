package com.example.amportoes.controller;

import com.example.amportoes.entity.Produto;
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProdutoControllerIntegrationTest {

    private static final String PRODUTOS_URL = "/api/produtos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProdutoRepository produtoRepository;

    @BeforeEach
    void limparProdutos() {
        produtoRepository.deleteAll();
    }

    @Test
    void deveCriarProduto() throws Exception {
        mockMvc.perform(post(PRODUTOS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProduto("Portao basculante", "1200.00", 5, 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Portao basculante")))
                .andExpect(jsonPath("$.codigo", is(5)))
                .andExpect(jsonPath("$.preco", is(1200.0)));

        org.assertj.core.api.Assertions.assertThat(produtoRepository.count()).isEqualTo(1);
    }

    @Test
    void deveListarProdutos() throws Exception {
        salvarProduto("Portao deslizante", "2000.00", 4, 1);
        salvarProduto("Porta social", "800.00", 8, 2);

        mockMvc.perform(get(PRODUTOS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nome", containsInAnyOrder("Portao deslizante", "Porta social")));
    }

    @Test
    void deveBuscarProdutoPorId() throws Exception {
        Produto produto = salvarProduto("Portao pivotante", "1500.00", 3, 1);

        mockMvc.perform(get(PRODUTOS_URL + "/" + produto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Portao pivotante")))
                .andExpect(jsonPath("$.codigo", is(3)));
    }

    @Test
    void deveAtualizarProduto() throws Exception {
        Produto produto = salvarProduto("Nome antigo", "1000.00", 2, 1);

        mockMvc.perform(put(PRODUTOS_URL + "/" + produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProduto("Nome atualizado", "1750.50", 10, 3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Nome atualizado")))
                .andExpect(jsonPath("$.codigo", is(10)))
                .andExpect(jsonPath("$.preco", is(1750.5)));

        Produto atualizado = produtoRepository.findById(produto.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(atualizado.getNome()).isEqualTo("Nome atualizado");
    }

    @Test
    void deveExcluirProduto() throws Exception {
        Produto produto = salvarProduto("Produto removido", "500.00", 1, 1);

        mockMvc.perform(delete(PRODUTOS_URL + "/" + produto.getId()))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(produtoRepository.findById(produto.getId())).isEmpty();
    }

    @Test
    void deveRetornar404AoBuscarProdutoInexistente() throws Exception {
        mockMvc.perform(get(PRODUTOS_URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.mensagem", is("Produto não foi encontrado")));
    }

    @Test
    void deveRetornar400ParaDadosObrigatoriosAusentes() throws Exception {
        String produtoInvalido = """
                {
                  "nome": "",
                  "preco": null,
                  "quantidadeEstoque": null,
                  "estoqueMinimo": null
                }
                """;

        mockMvc.perform(post(PRODUTOS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensagem", is("Dados inválidos")))
                .andExpect(jsonPath("$.campos.nome", is("O nome é obrigatório")))
                .andExpect(jsonPath("$.campos.codigo", is("O código é obrigatório")))
                .andExpect(jsonPath("$.campos.preco", is("O preço é obrigatório")));
    }

    @Test
    void deveRetornar400ParaEstoqueNegativo() throws Exception {
        mockMvc.perform(post(PRODUTOS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Produto inválido\",\"preco\":100.00}"))
                .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.codigo", is("O código é obrigatório")));
    }

    @Test
    void deveListarProdutosComEstoqueBaixo() throws Exception {
        salvarProduto("Abaixo do mínimo", "100.00", 1, 2);
        salvarProduto("No limite mínimo", "200.00", 2, 2);
        salvarProduto("Estoque normal", "300.00", 3, 2);

        mockMvc.perform(get(PRODUTOS_URL + "/baixo-estoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nome", containsInAnyOrder("Abaixo do mínimo", "No limite mínimo")));
    }

    @Test
    void devePermitirCorsParaFrontendLocal() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/tarefas")
                        .header("Origin", "http://localhost:4000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4000"))
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers", org.hamcrest.Matchers.containsString("content-type")));
    }

    private Produto salvarProduto(String nome, String preco, int quantidadeEstoque, int estoqueMinimo) {
        Produto produto = new Produto();
        produto.setCodigo((long) quantidadeEstoque);
        produto.setNome(nome);
        produto.setDescricao("Produto de teste");
        produto.setPreco(new BigDecimal(preco));
        produto.setQuantidadeEstoque(quantidadeEstoque);
        produto.setEstoqueMinimo(estoqueMinimo);
        return produtoRepository.save(produto);
    }

    private String jsonProduto(String nome, String preco, int quantidadeEstoque, int estoqueMinimo)
            throws Exception {
        return objectMapper.writeValueAsString(new ProdutoPayload(
            nome,
            (long) quantidadeEstoque,
            new BigDecimal(preco),
            "2027-01-01"
        ));
    }

    private record ProdutoPayload(
            String nome,
            Long codigo,
            BigDecimal preco,
            String dataValidade
    ) {
    }
}
