package com.example.amportoes.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;

class ErroSegurancaHandlerTest {

    private final ErroSegurancaHandler handler = new ErroSegurancaHandler();

    @Test
    void deveResponder401ComCorpoPadronizado() throws Exception {
        // Given
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        // When
        handler.commence(new MockHttpServletRequest(), resposta, new BadCredentialsException("x"));

        // Then
        assertEquals(401, resposta.getStatus());
        assertTrue(resposta.getContentType().startsWith("application/json"));
        String corpo = resposta.getContentAsString();
        assertTrue(corpo.contains("\"status\":401"));
        assertTrue(corpo.contains("\"erro\":\"Unauthorized\""));
        assertTrue(corpo.contains("\"mensagem\""));
    }

    @Test
    void deveResponder403ComCorpoPadronizado() throws Exception {
        // Given
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        // When
        handler.handle(new MockHttpServletRequest(), resposta, new AccessDeniedException("negado"));

        // Then
        assertEquals(403, resposta.getStatus());
        assertTrue(resposta.getContentType().startsWith("application/json"));
        String corpo = resposta.getContentAsString();
        assertTrue(corpo.contains("\"status\":403"));
        assertTrue(corpo.contains("\"erro\":\"Forbidden\""));
    }
}