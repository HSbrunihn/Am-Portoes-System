package com.example.amportoes.controller;

import com.example.amportoes.controller.request.OrcamentoRequest;
import com.example.amportoes.controller.response.OrcamentoResponse;
import com.example.amportoes.service.OrcamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos")
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrcamentoResponse criar(@RequestBody @Valid OrcamentoRequest request) {
        return orcamentoService.criar(request);
    }

    @GetMapping
    public List<OrcamentoResponse> listar() {
        return orcamentoService.listar();
    }

    @GetMapping("/{id}")
    public OrcamentoResponse buscarPorId(@PathVariable Long id) {
        return orcamentoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public OrcamentoResponse atualizar(@PathVariable Long id, @RequestBody @Valid OrcamentoRequest request) {
        return orcamentoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        orcamentoService.deletar(id);
    }
}
