
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

import static org.assertj.core.api.Assertions.assertThat;
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
                .andExpect(jsonPath("$.status", is("PENDENTE")))
                .andExpect(jsonPath("$.valor", is(1500.0)));

        assertThat(orcamentoRepository.count()).isEqualTo(1);

        Orcamento salvo = orcamentoRepository.findAll().get(0);

        assertThat(salvo.getTitulo()).isEqualTo("Instalação");
        assertThat(salvo.getValor()).isEqualByComparingTo("1500.00");
        assertThat(salvo.getStatus()).isEqualTo(StatusTarefa.PENDENTE);
    }

    @Test
    void deveListarOrcamentos() throws Exception {
        salvar("Instalação");
        salvar("Manutenção");

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].titulo",
                        containsInAnyOrder("Instalação", "Manutenção")));
    }

    @Test
    void deveFiltrarPorStatus() throws Exception {
        salvar("Instalação");

        mockMvc.perform(get(URL).param("status", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("PENDENTE")))
                .andExpect(jsonPath("$[0].titulo", is("Instalação")));
    }

    @Test
    void deveBuscarPorId() throws Exception {
        Orcamento orcamento = salvar("Instalação");

        mockMvc.perform(get(URL + "/" + orcamento.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is("Instalação")))
                .andExpect(jsonPath("$.valor", is(1500.0)))
                .andExpect(jsonPath("$.status", is("PENDENTE")));
    }

    @Test
    void deveRetornar404ParaOrcamentoInexistente() throws Exception {
        mockMvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound());

        assertThat(orcamentoRepository.count()).isZero();
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

        Orcamento atualizado = orcamentoRepository
                .findById(orcamento.getId())
                .orElseThrow();

        assertThat(atualizado.getTitulo()).isEqualTo("Título atualizado");
        assertThat(atualizado.getValor()).isEqualByComparingTo("2000.00");
    }

    @Test
    void deveExcluirOrcamento() throws Exception {
        Orcamento orcamento = salvar("Instalação");

        mockMvc.perform(delete(URL + "/" + orcamento.getId()))
                .andExpect(status().isNoContent());

        assertThat(
                orcamentoRepository.findById(orcamento.getId())
        ).isEmpty();

        assertThat(orcamentoRepository.count()).isZero();
    }

    @Test
    void deveRejeitarOrcamentoInvalido() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        assertThat(orcamentoRepository.count()).isZero();
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
