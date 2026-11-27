package com.example.amportoes.controller;

import com.example.amportoes.controller.request.LoginRequest;
import com.example.amportoes.controller.response.LoginResponse;
import com.example.amportoes.controller.response.UsuarioResponse;
import com.example.amportoes.entity.Usuario;
import com.example.amportoes.repository.UsuarioRepository;
import com.example.amportoes.security.JwtService;
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

    public AuthController(UsuarioRepository usuarios,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(request.email())
                .filter(item -> passwordEncoder.matches(request.senha(), item.getSenha()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos"));

        String token = jwtService.gerarToken(usuario.getEmail());

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.expiracaoSegundos(),
                UsuarioResponse.fromEntity(usuario));
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
}