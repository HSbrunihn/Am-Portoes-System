package com.example.amportoes.security;

import com.example.amportoes.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Não é @Component de propósito: é criado dentro do SecurityConfig
// para não ser registrado duas vezes pelo Spring Boot.
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarios;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarios) {
        this.jwtService = jwtService;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String cabecalho = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            String token = cabecalho.substring(PREFIXO.length()).trim();

            if (jwtService.tokenValido(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String email = jwtService.extrairEmail(token);

                // O usuário do token ainda precisa existir no banco
                if (usuarios.findByEmailIgnoreCase(email).isPresent()) {
                    UsernamePasswordAuthenticationToken autenticacao =
                            new UsernamePasswordAuthenticationToken(email, null, List.of());
                    autenticacao.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(autenticacao);
                }
            }
        }

        // Sempre segue a cadeia. Quem decide o 401 é o SecurityConfig,
        // não o filtro: sem autenticação, a rota protegida é recusada lá.
        chain.doFilter(request, response);
    }
}