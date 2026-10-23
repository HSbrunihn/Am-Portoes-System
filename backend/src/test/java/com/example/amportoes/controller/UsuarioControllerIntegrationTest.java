
package com.example.amportoes.controller;

import com.example.amportoes.controller.request.UsuarioRequest;
import com.example.amportoes.entity.Usuario;
import com.example.amportoes.repository.UsuarioRepository;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerIntegrationTest {

    private static final String URL = "/api/usuarios";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void limparBanco() {
        usuarioRepository.deleteAll();
    }

    @Test
    void deveCriarUsuario() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Bruno", "bruno@teste.com", "senha123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Bruno")))
                .andExpect(jsonPath("$.email", is("bruno@teste.com")))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void deveListarUsuarios() throws Exception {
        salvar("Bruno", "bruno@teste.com");
        salvar("Ana", "ana@teste.com");

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void deveBuscarUsuarioPorId() throws Exception {
        Usuario usuario = salvar("Bruno", "bruno@teste.com");

        mockMvc.perform(get(URL + "/" + usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Bruno")))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void deveRetornar404ParaUsuarioInexistente() throws Exception {
        mockMvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarUsuario() throws Exception {
        Usuario usuario = salvar("Bruno", "bruno@teste.com");

        mockMvc.perform(put(URL + "/" + usuario.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Bruno Atualizado",
                                "novo@teste.com", "senha456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Bruno Atualizado")))
                .andExpect(jsonPath("$.email", is("novo@teste.com")));
    }

    @Test
    void deveExcluirUsuario() throws Exception {
        Usuario usuario = salvar("Bruno", "bruno@teste.com");

        mockMvc.perform(delete(URL + "/" + usuario.getId()))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(
                usuarioRepository.findById(usuario.getId())).isEmpty();
    }

    @Test
    void deveRejeitarUsuarioInvalido() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private Usuario salvar(String nome, String email) throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(nome, email, "senha123")))
                .andExpect(status().isCreated());

        return usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
    }

    private String json(String nome, String email, String senha)
            throws Exception {
        return mapper.writeValueAsString(
                new UsuarioRequest(nome, email, senha)
        );
    }
}
