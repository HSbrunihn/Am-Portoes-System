package com.example.amportoes.controller;

import com.example.amportoes.controller.request.LoginRequest;
import com.example.amportoes.controller.response.UsuarioResponse;
import com.example.amportoes.entity.Usuario;
import com.example.amportoes.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public UsuarioResponse login(@RequestBody @Valid LoginRequest request, HttpSession session) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(request.email())
                .filter(item -> passwordEncoder.matches(request.senha(), item.getSenha()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos"));
        session.setAttribute("usuarioId", usuario.getId());
        return UsuarioResponse.fromEntity(usuario);
    }

    @GetMapping("/me")
    public UsuarioResponse me(HttpSession session) {
        Object id = session.getAttribute("usuarioId");
        if (!(id instanceof Long usuarioId)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return usuarios.findById(usuarioId).map(UsuarioResponse::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) { session.invalidate(); }
}
