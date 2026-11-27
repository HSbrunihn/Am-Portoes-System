package com.example.amportoes.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class ErroSegurancaHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    // 401: não sei quem você é (sem token, token inválido ou expirado)
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        escrever(response, HttpStatus.UNAUTHORIZED,
                "Autenticação necessária: envie um token válido");
    }

    // 403: sei quem você é, mas você não tem permissão
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        escrever(response, HttpStatus.FORBIDDEN,
                "Você não tem permissão para acessar este recurso");
    }

    private void escrever(HttpServletResponse response, HttpStatus status, String mensagem)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String corpo = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"erro\":\"%s\",\"mensagem\":\"%s\"}",
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensagem);

        response.getWriter().write(corpo);
    }
}