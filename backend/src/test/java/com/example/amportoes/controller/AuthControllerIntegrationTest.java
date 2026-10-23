package com.example.amportoes.controller;

import com.example.amportoes.controller.request.LoginRequest;
import com.example.amportoes.entity.Usuario;
import com.example.amportoes.repository.UsuarioRepository;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    private static final String URL = "/api/auth";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparBanco() {
        usuarioRepository.deleteAll();
    }

    @Test
    void deveRealizarLoginComCredenciaisValidas() throws Exception {
        criarUsuario();

        mockMvc.perform(post(URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("bruno@teste.com", "senha123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("bruno@teste.com")))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void deveRecusarSenhaIncorreta() throws Exception {
        criarUsuario();

        mockMvc.perform(post(URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("bruno@teste.com", "senhaerrada")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRecusarUsuarioInexistente() throws Exception {
        mockMvc.perform(post(URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("inexistente@teste.com", "senha123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRecusarConsultaMeSemLogin() throws Exception {
        mockMvc.perform(get(URL + "/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornarUsuarioAutenticadoEmMe() throws Exception {
        criarUsuario();

        MockHttpSession session = (MockHttpSession) mockMvc.perform(
                        post(URL + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json("bruno@teste.com", "senha123")))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(get(URL + "/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("bruno@teste.com")));
    }

    @Test
    void deveEncerrarSessaoAoFazerLogout() throws Exception {
        criarUsuario();

        MockHttpSession session = (MockHttpSession) mockMvc.perform(
                        post(URL + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json("bruno@teste.com", "senha123")))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post(URL + "/logout").session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(URL + "/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    private void criarUsuario() {
        Usuario usuario = new Usuario(
                null,
                "Bruno",
                "bruno@teste.com",
                passwordEncoder.encode("senha123")
        );

        usuarioRepository.save(usuario);
    }

    private String json(String email, String senha) throws Exception {
        return mapper.writeValueAsString(new LoginRequest(email, senha));
    }
}