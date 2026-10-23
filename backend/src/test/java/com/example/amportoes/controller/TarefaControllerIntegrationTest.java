
package com.example.amportoes.controller;

import com.example.amportoes.controller.request.TarefaRequest;
import com.example.amportoes.entity.StatusTarefa;
import com.example.amportoes.entity.Tarefa;
import com.example.amportoes.repository.TarefaRepository;
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
class TarefaControllerIntegrationTest {

    private static final String URL = "/api/tarefas";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TarefaRepository tarefaRepository;

    @BeforeEach
    void limparBanco() {
        tarefaRepository.deleteAll();
    }

    @Test
    void deveCriarTarefa() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Instalar portão", "500.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo", is("Instalar portão")))
                .andExpect(jsonPath("$.valor", is(500.0)))
                .andExpect(jsonPath("$.status", is("PENDENTE")));

        assertThat(tarefaRepository.count()).isEqualTo(1);

        Tarefa salva = tarefaRepository.findAll().get(0);

        assertThat(salva.getTitulo()).isEqualTo("Instalar portão");
        assertThat(salva.getValor()).isEqualByComparingTo("500.00");
        assertThat(salva.getStatus()).isEqualTo(StatusTarefa.PENDENTE);
    }

    @Test
    void deveListarTarefas() throws Exception {
        salvar("Instalar portão");
        salvar("Realizar manutenção");

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].titulo",
                        containsInAnyOrder(
                                "Instalar portão",
                                "Realizar manutenção"
                        )));
    }

    @Test
    void deveFiltrarTarefasPorStatus() throws Exception {
        salvar("Instalar portão");

        mockMvc.perform(get(URL).param("status", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo", is("Instalar portão")))
                .andExpect(jsonPath("$[0].status", is("PENDENTE")));
    }

    @Test
    void deveBuscarTarefaPorId() throws Exception {
        Tarefa tarefa = salvar("Instalar portão");

        mockMvc.perform(get(URL + "/" + tarefa.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is("Instalar portão")))
                .andExpect(jsonPath("$.valor", is(500.0)))
                .andExpect(jsonPath("$.status", is("PENDENTE")));
    }

    @Test
    void deveRetornar404ParaTarefaInexistente() throws Exception {
        mockMvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound());

        assertThat(tarefaRepository.count()).isZero();
    }

    @Test
    void deveAtualizarTarefa() throws Exception {
        Tarefa tarefa = salvar("Título antigo");

        mockMvc.perform(put(URL + "/" + tarefa.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Título atualizado", "800.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is("Título atualizado")))
                .andExpect(jsonPath("$.valor", is(800.0)));

        Tarefa atualizada = tarefaRepository
                .findById(tarefa.getId())
                .orElseThrow();

        assertThat(atualizada.getTitulo()).isEqualTo("Título atualizado");
        assertThat(atualizada.getValor()).isEqualByComparingTo("800.00");
    }

    @Test
    void deveAtualizarStatus() throws Exception {
        Tarefa tarefa = salvar("Instalar portão");

        mockMvc.perform(patch(URL + "/" + tarefa.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"CONCLUIDA\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONCLUIDA")));

        Tarefa atualizada = tarefaRepository
                .findById(tarefa.getId())
                .orElseThrow();

        assertThat(atualizada.getStatus()).isEqualTo(StatusTarefa.CONCLUIDA);
    }

    @Test
    void deveExcluirTarefa() throws Exception {
        Tarefa tarefa = salvar("Instalar portão");

        mockMvc.perform(delete(URL + "/" + tarefa.getId()))
                .andExpect(status().isNoContent());

        assertThat(tarefaRepository.findById(tarefa.getId())).isEmpty();
        assertThat(tarefaRepository.count()).isZero();
    }

    @Test
    void deveRejeitarTarefaInvalida() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        assertThat(tarefaRepository.count()).isZero();
    }

    private Tarefa salvar(String titulo) {
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(titulo);
        tarefa.setDescricao("Tarefa de teste");
        tarefa.setValor(new BigDecimal("500.00"));
        tarefa.setDataVencimento(LocalDate.of(2027, 1, 1));
        tarefa.setStatus(StatusTarefa.PENDENTE);

        return tarefaRepository.save(tarefa);
    }

    private String json(String titulo, String valor) throws Exception {
        return mapper.writeValueAsString(new TarefaRequest(
                titulo,
                "Tarefa de teste",
                new BigDecimal(valor),
                LocalDate.of(2027, 1, 1)
        ));
    }
}
