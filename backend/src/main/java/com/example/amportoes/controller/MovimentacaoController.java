
package com.example.amportoes.controller;

import com.example.amportoes.dto.MovimentacaoDTO;
import com.example.amportoes.dto.MovimentacaoResponseDTO;
import com.example.amportoes.service.MovimentacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(
            MovimentacaoService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoResponseDTO registrarMovimentacao(
            @RequestBody @Valid MovimentacaoDTO dto) {
        return movimentacaoService.registrarMovimentacao(dto);
    }

    @GetMapping
    public List<MovimentacaoResponseDTO> listarMovimentacoes() {
        return movimentacaoService.listarMovimentacoes();
    }

    @GetMapping("/produto/{produtoId}")
    public List<MovimentacaoResponseDTO> listarPorProduto(
            @PathVariable Long produtoId) {
        return movimentacaoService.listarPorProduto(produtoId);
    }
}
