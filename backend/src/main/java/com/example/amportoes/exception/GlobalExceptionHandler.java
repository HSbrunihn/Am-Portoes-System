package com.example.amportoes.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> tratarRegraNegocio(RegraNegocioException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<Map<String, Object>> tratarConflito(ConflitoException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                campos.put(erro.getField(), erro.getDefaultMessage()));

        Map<String, Object> corpo = corpoErro(HttpStatus.BAD_REQUEST, "Dados inválidos");
        corpo.put("campos", campos);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> tratarJsonIlegivel(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou malformado");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tratarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + ex.getName() + "'");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> tratarIntegridade(DataIntegrityViolationException ex) {
        return resposta(HttpStatus.CONFLICT,
                "A operação viola uma restrição de dados (registro duplicado ou em uso)");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> tratarNaoAutenticado(AuthenticationException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, "Autenticação necessária: envie um token válido");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> tratarAcessoNegado(AccessDeniedException ex) {
        return resposta(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso");
    }

    // Rede de segurança: exceções do próprio Spring MVC (404, 405, 415, ResponseStatusException...)
    // mantêm o status delas; qualquer outra coisa vira 500 sem vazar detalhes.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> tratarGenerico(Exception ex) {
        if (ex instanceof ErrorResponse erroSpring) {
            HttpStatusCode status = erroSpring.getStatusCode();
            String detalhe = erroSpring.getBody().getDetail();
            String mensagem = (detalhe == null || detalhe.isBlank())
                    ? "A requisição não pôde ser atendida"
                    : detalhe;
            return ResponseEntity.status(status).body(corpoErro(status, mensagem));
        }

        log.error("Erro inesperado", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(corpoErro(status, mensagem));
    }

    private Map<String, Object> corpoErro(HttpStatusCode status, String mensagem) {
        HttpStatus conhecido = HttpStatus.resolve(status.value());

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", status.value());
        corpo.put("erro", conhecido != null ? conhecido.getReasonPhrase() : "Erro");
        corpo.put("mensagem", mensagem);
        return corpo;
    }
}