package com.example.amportoes.controller;

import com.example.amportoes.dto.MovimentacaoDTO;
import com.example.amportoes.entity.MovimentacaoEstoque;
import com.example.amportoes.service.MovimentacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(MovimentacaoService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoEstoque registrarMovimentacao(@RequestBody @Valid MovimentacaoDTO dto) {
        return movimentacaoService.registrarMovimentacao(dto);
    }

    @ExceptionHandler(IllegalArgumentException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public Map<String, String> handleIllegalArgument(IllegalArgumentException ex) {
    return Map.of("mensagem", ex.getMessage());
}
}