package com.example.amportoes.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SEGREDO = Base64.getEncoder().encodeToString(
            "segredo-de-teste-com-mais-de-32-bytes!!".getBytes(StandardCharsets.UTF_8));

    private static final String OUTRO_SEGREDO = Base64.getEncoder().encodeToString(
            "outro-segredo-totalmente-diferente-123456".getBytes(StandardCharsets.UTF_8));

    private final JwtService jwtService = new JwtService(SEGREDO, 60);

    @Test
    void deveGerarTokenEExtrairOEmail() {
        // When
        String token = jwtService.gerarToken("bruno@example.com");

        // Then
        assertTrue(jwtService.tokenValido(token));
        assertEquals("bruno@example.com", jwtService.extrairEmail(token));
    }

    @Test
    void deveRejeitarTokenAssinadoComOutraChave() {
        // Given
        String tokenDeOutroServidor = new JwtService(OUTRO_SEGREDO, 60).gerarToken("bruno@example.com");

        // When / Then
        assertFalse(jwtService.tokenValido(tokenDeOutroServidor));
    }

    @Test
    void deveRejeitarTokenComDadosAdulterados() {
        // Given: troca o conteúdo do token mantendo a assinatura original
        String[] partes = jwtService.gerarToken("bruno@example.com").split("\\.");
        String dadosFalsos = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"hacker@example.com\"}".getBytes(StandardCharsets.UTF_8));
        String adulterado = partes[0] + "." + dadosFalsos + "." + partes[2];

        // When / Then
        assertFalse(jwtService.tokenValido(adulterado));
    }

    @Test
    void deveRejeitarTokenExpirado() {
        // Given: expiração negativa gera um token que já nasce vencido
        String vencido = new JwtService(SEGREDO, -1).gerarToken("bruno@example.com");

        // When / Then
        assertFalse(jwtService.tokenValido(vencido));
    }

    @Test
    void deveRejeitarTextoQueNaoEhToken() {
        assertFalse(jwtService.tokenValido("isto-nao-e-um-jwt"));
        assertFalse(jwtService.tokenValido(" "));
        assertFalse(jwtService.tokenValido(null));
    }

    @Test
    void deveRecusarChaveCurtaDemais() {
        String chaveCurta = Base64.getEncoder().encodeToString("curta".getBytes(StandardCharsets.UTF_8));

        assertThrows(RuntimeException.class, () -> new JwtService(chaveCurta, 60));
    }

    @Test
    void deveConverterMinutosParaSegundos() {
        assertEquals(3600, jwtService.expiracaoSegundos());
    }
}