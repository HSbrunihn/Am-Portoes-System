package com.example.amportoes.security;

import com.example.amportoes.controller.AuthController;
import com.example.amportoes.entity.Usuario;
import com.example.amportoes.repository.UsuarioRepository;
import com.example.amportoes.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, ErroSegurancaHandler.class})
class SecurityConfigIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UsuarioRepository usuarioRepository;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    UsuarioService usuarioService;

    @Test
    void givenTokenValidoNoCabecalhoQuandoAcessaRotaProtegidaEntaoRetorna200() throws Exception {
        // Given
        when(jwtService.tokenValido("token-bom")).thenReturn(true);
        when(jwtService.extrairEmail("token-bom")).thenReturn("bruno@example.com");
        when(usuarioRepository.findByEmailIgnoreCase("bruno@example.com"))
                .thenReturn(Optional.of(new Usuario(2L, "Bruno", "bruno@example.com", "hash")));

        // When / Then
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer token-bom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bruno"));
    }

    @Test
    void givenTokenInvalidoNoCabecalhoQuandoAcessaRotaProtegidaEntaoRetorna401() throws Exception {
        // When / Then
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer token-ruim"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void givenOrigemDoAngularQuandoFazPreflightEntaoPermiteOAcesso() throws Exception {
        // When / Then
        mockMvc.perform(options("/api/auth/me")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void givenOrigemDesconhecidaQuandoFazPreflightEntaoRecusa() throws Exception {
        // When / Then
        mockMvc.perform(options("/api/auth/me")
                        .header("Origin", "http://site-estranho.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}