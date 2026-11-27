package com.example.amportoes.controller;

import com.example.amportoes.entity.Usuario;
import com.example.amportoes.repository.UsuarioRepository;
import com.example.amportoes.security.ErroSegurancaHandler;
import com.example.amportoes.security.JwtService;
import com.example.amportoes.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, ErroSegurancaHandler.class})
class AuthControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UsuarioRepository usuarioRepository;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    JwtService jwtService;

    @Test
    void givenCredenciaisValidasQuandoFazLoginEntaoRetornaTokenEUsuarioSemSenha() throws Exception {
        // Given
        Usuario usuario = new Usuario(2L, "Bruno", "bruno@example.com", "hash");

        when(usuarioRepository.findByEmailIgnoreCase("bruno@example.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "hash")).thenReturn(true);
        when(jwtService.gerarToken("bruno@example.com")).thenReturn("token-de-teste");
        when(jwtService.expiracaoSegundos()).thenReturn(3600L);

        // When / Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bruno@example.com\",\"senha\":\"senha123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-de-teste"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEmSegundos").value(3600))
                .andExpect(jsonPath("$.usuario.nome").value("Bruno"))
                .andExpect(jsonPath("$.usuario.senha").doesNotExist());
    }

    @Test
    void givenSenhaIncorretaQuandoFazLoginEntaoRetorna401SemGerarToken() throws Exception {
        // Given
        when(usuarioRepository.findByEmailIgnoreCase("bruno@example.com"))
                .thenReturn(Optional.of(
                        new Usuario(2L, "Bruno", "bruno@example.com", "hash")));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        // When / Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bruno@example.com\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());

        verify(jwtService, never()).gerarToken(anyString());
    }

    @Test
    void givenEmailInexistenteQuandoFazLoginEntaoRetorna401() throws Exception {
        // Given
        when(usuarioRepository.findByEmailIgnoreCase("ninguem@example.com"))
                .thenReturn(Optional.empty());

        // When / Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ninguem@example.com\",\"senha\":\"senha123\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void givenEmailInvalidoQuandoFazLoginEntaoRetorna400ComErroDeValidacao() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalido\",\"senha\":\"senha123\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioRepository, passwordEncoder, jwtService);
    }

    @Test
    void givenSemTokenQuandoConsultaMeEntaoRetorna401ComCorpoPadronizado() throws Exception {
        // When / Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "bruno@example.com")
    void givenUsuarioAutenticadoQuandoConsultaMeEntaoRetornaUsuario() throws Exception {
        // Given
        when(usuarioRepository.findByEmailIgnoreCase("bruno@example.com"))
                .thenReturn(Optional.of(
                        new Usuario(2L, "Bruno", "bruno@example.com", "hash")));

        // When / Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bruno"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @WithMockUser(username = "fantasma@example.com")
    void givenTokenDeUsuarioQueNaoExisteMaisQuandoConsultaMeEntaoRetorna401() throws Exception {
        // Given
        when(usuarioRepository.findByEmailIgnoreCase("fantasma@example.com"))
                .thenReturn(Optional.empty());

        // When / Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void givenUsuarioAutenticadoQuandoFazLogoutEntaoRetorna204() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenSemTokenQuandoFazLogoutEntaoRetorna401() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }
}