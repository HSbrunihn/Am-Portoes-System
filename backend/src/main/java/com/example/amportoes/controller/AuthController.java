package com.example.amportoes.controller;

import com.example.amportoes.controller.request.LoginRequest;
import com.example.amportoes.controller.request.UsuarioRequest;
import com.example.amportoes.controller.response.LoginResponse;
import com.example.amportoes.controller.response.UsuarioResponse;
import com.example.amportoes.entity.Usuario;
import com.example.amportoes.repository.UsuarioRepository;
import com.example.amportoes.security.JwtService;
import com.example.amportoes.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthController(UsuarioRepository usuarios,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          UsuarioService usuarioService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(request.email())
                .filter(item -> passwordEncoder.matches(request.senha(), item.getSenha()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos"));

        return respostaComToken(UsuarioResponse.fromEntity(usuario));
    }

    // Cadastro público: cria a conta (validação e e-mail duplicado ficam no service) e já devolve o token
    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse cadastro(@RequestBody @Valid UsuarioRequest request) {
        return respostaComToken(usuarioService.criar(request));
    }

    // O filtro JWT já identificou o usuário pelo token; aqui só devolvemos os dados dele
    @GetMapping("/me")
    public UsuarioResponse me(Authentication authentication) {
        return usuarios.findByEmailIgnoreCase(authentication.getName())
                .map(UsuarioResponse::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    // Sem sessão no servidor, não há o que invalidar: o cliente descarta o token
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        // intencionalmente vazio
    }

    private LoginResponse respostaComToken(UsuarioResponse usuario) {
        return new LoginResponse(
                jwtService.gerarToken(usuario.email()),
                "Bearer",
                jwtService.expiracaoSegundos(),
                usuario);
    }
}
