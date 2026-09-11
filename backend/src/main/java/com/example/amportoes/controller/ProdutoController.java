package com.example.amportoes.controller;

import com.example.amportoes.dto.ProdutoDTO;
import com.example.amportoes.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoDTO criar(@RequestBody @Valid ProdutoDTO dto) {
        return produtoService.criar(dto);
    }

    @GetMapping
    public List<ProdutoDTO> listar() {
        return produtoService.listar();
    }

    @GetMapping("/{id}")
    public ProdutoDTO buscarPorId(@PathVariable Long id) {
        return produtoService.buscarPorIdDTO(id);
    }

    @PutMapping("/{id}")
    public ProdutoDTO atualizar(@PathVariable Long id, @RequestBody @Valid ProdutoDTO dto) {
        return produtoService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        produtoService.excluir(id);
    }

    @GetMapping("/baixo-estoque")
    public List<ProdutoDTO> listarProdutosComBaixoEstoque() {
        return produtoService.listarProdutosComBaixoEstoque();
    }
}
