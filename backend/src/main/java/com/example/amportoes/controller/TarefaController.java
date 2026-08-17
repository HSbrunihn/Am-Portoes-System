package com.example.amportoes.controller;

import com.example.amportoes.controller.request.TarefaRequest;
import com.example.amportoes.controller.response.TarefaResponse;
import com.example.amportoes.entity.StatusTarefa;
import com.example.amportoes.service.TarefaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaResponse criar(@RequestBody @Valid TarefaRequest request) {
        return tarefaService.criar(request);
    }

    @GetMapping
    public List<TarefaResponse> listar(@RequestParam(required = false) StatusTarefa status) {
        return tarefaService.listar(status);
    }

    @GetMapping("/{id}")
    public TarefaResponse buscarPorId(@PathVariable Long id) {
        return tarefaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public TarefaResponse atualizar(@PathVariable Long id, @RequestBody @Valid TarefaRequest request) {
        return tarefaService.atualizar(id, request);
    }

    @PatchMapping("/{id}/status")
    public TarefaResponse atualizarStatus(@PathVariable Long id, @RequestBody StatusTarefa status) {
        return tarefaService.atualizarStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        tarefaService.deletar(id);
    }
}
