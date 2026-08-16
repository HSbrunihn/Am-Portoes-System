package com.example.amportoes.controller.request;

import com.example.amportoes.entity.Tarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TarefaRequest(
        @NotBlank(message = "O título é obrigatório") String titulo,
        String descricao,
        @NotNull(message = "A data de vencimento é obrigatória") LocalDate dataVencimento
) {
    public Tarefa toEntity() {
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(this.titulo());
        tarefa.setDescricao(this.descricao());
        tarefa.setDataVencimento(this.dataVencimento());
        return tarefa;
    }
}
