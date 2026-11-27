package com.example.amportoes.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveResponder409ParaConflito() {
        ResponseEntity<Map<String, Object>> resposta =
                handler.tratarConflito(new ConflitoException("E-mail repetido"));

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertEquals("E-mail repetido", resposta.getBody().get("mensagem"));
        assertEquals(409, resposta.getBody().get("status"));
        assertEquals("Conflict", resposta.getBody().get("erro"));
    }

    @Test
    void deveResponder400ParaJsonIlegivel() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("json quebrado", mock(HttpInputMessage.class));

        ResponseEntity<Map<String, Object>> resposta = handler.tratarJsonIlegivel(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals("Corpo da requisição inválido ou malformado", resposta.getBody().get("mensagem"));
    }

    @Test
    void deveResponder400ParaParametroComTipoInvalido() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");

        ResponseEntity<Map<String, Object>> resposta = handler.tratarTipoInvalido(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals("Valor inválido para o parâmetro 'id'", resposta.getBody().get("mensagem"));
    }

    @Test
    void deveResponder409ParaViolacaoDeIntegridade() {
        ResponseEntity<Map<String, Object>> resposta =
                handler.tratarIntegridade(new DataIntegrityViolationException("detalhe interno do banco"));

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertFalse(resposta.getBody().get("mensagem").toString().contains("detalhe interno"));
    }

    @Test
    void deveResponder401ParaFalhaDeAutenticacao() {
        ResponseEntity<Map<String, Object>> resposta =
                handler.tratarNaoAutenticado(new BadCredentialsException("x"));

        assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
    }

    @Test
    void deveResponder403ParaAcessoNegado() {
        ResponseEntity<Map<String, Object>> resposta =
                handler.tratarAcessoNegado(new AccessDeniedException("negado"));

        assertEquals(HttpStatus.FORBIDDEN, resposta.getStatusCode());
    }

    @Test
    void deveManterStatusEMotivoDeResponseStatusException() throws Exception {
        ResponseEntity<Map<String, Object>> resposta = handler.tratarGenerico(
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos"));

        assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
        assertEquals("E-mail ou senha inválidos", resposta.getBody().get("mensagem"));
    }

    @Test
    void deveUsarMensagemPadraoQuandoResponseStatusExceptionNaoTemMotivo() throws Exception {
        ResponseEntity<Map<String, Object>> resposta =
                handler.tratarGenerico(new ResponseStatusException(HttpStatus.NOT_FOUND));

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertEquals("A requisição não pôde ser atendida", resposta.getBody().get("mensagem"));
    }

    @Test
    void deveManterStatus405DasExcecoesDoSpringMvc() throws Exception {
        ResponseEntity<Map<String, Object>> resposta =
                handler.tratarGenerico(new HttpRequestMethodNotSupportedException("POST"));

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, resposta.getStatusCode());
        assertEquals(405, resposta.getBody().get("status"));
    }

    @Test
    void deveResponder500SemVazarDetalhesDeErroInesperado() throws Exception {
        ResponseEntity<Map<String, Object>> resposta =
                handler.tratarGenerico(new RuntimeException("boom: senha do banco errada"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.getStatusCode());
        assertEquals("Erro interno do servidor", resposta.getBody().get("mensagem"));
        assertFalse(resposta.getBody().toString().contains("boom"));
    }
}