
package com.example.amportoes.controller;

import com.example.amportoes.controller.request.OrcamentoRequest;
import com.example.amportoes.entity.Orcamento;
import com.example.amportoes.entity.StatusTarefa;
import com.example.amportoes.repository.OrcamentoRepository;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrcamentoControllerIntegrationTest {

    private static final String URL = "/api/orcamentos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @BeforeEach
    void limparBanco() {
        orcamentoRepository.deleteAll();
    }

    @Test
    void deveCriarOrcamento() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Instalação", "1500.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo", is("Instalação")))
                .andExpect(jsonPath("$.status", is("PENDENTE")));
    }

    @Test
    void deveListarOrcamentos() throws Exception {
        salvar("Instalação");
        salvar("Manutenção");

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void deveFiltrarPorStatus() throws Exception {
        salvar("Instalação");

        mockMvc.perform(get(URL).param("status", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("PENDENTE")));
    }

    @Test
    void deveBuscarPorId() throws Exception {
        Orcamento orcamento = salvar("Instalação");

        mockMvc.perform(get(URL + "/" + orcamento.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is("Instalação")));
    }

    @Test
    void deveRetornar404ParaOrcamentoInexistente() throws Exception {
        mockMvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarOrcamento() throws Exception {
        Orcamento orcamento = salvar("Título antigo");

        mockMvc.perform(put(URL + "/" + orcamento.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Título atualizado", "2000.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is("Título atualizado")))
                .andExpect(jsonPath("$.valor", is(2000.0)));
    }

    @Test
    void deveExcluirOrcamento() throws Exception {
        Orcamento orcamento = salvar("Instalação");

        mockMvc.perform(delete(URL + "/" + orcamento.getId()))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(
                orcamentoRepository.findById(orcamento.getId())).isEmpty();
    }

    @Test
    void deveRejeitarOrcamentoInvalido() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private Orcamento salvar(String titulo) {
        Orcamento orcamento = new Orcamento();
        orcamento.setTitulo(titulo);
        orcamento.setDescricao("Orçamento de teste");
        orcamento.setValor(new BigDecimal("1500.00"));
        orcamento.setDataVencimento(LocalDate.of(2027, 1, 1));
        orcamento.setStatus(StatusTarefa.PENDENTE);

        return orcamentoRepository.save(orcamento);
    }

    private String json(String titulo, String valor) throws Exception {
        return mapper.writeValueAsString(new OrcamentoRequest(
                titulo,
                "Orçamento de teste",
                new BigDecimal(valor),
                LocalDate.of(2027, 1, 1)
        ));
    }
}
