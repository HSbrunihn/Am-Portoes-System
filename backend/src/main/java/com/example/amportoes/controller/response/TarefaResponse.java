package com.example.amportoes.controller.response;

import com.example.amportoes.entity.StatusTarefa;
import com.example.amportoes.entity.Tarefa;

import java.time.LocalDate;

public record TarefaResponse(
        Long id,
        String titulo,
        String descricao,
        LocalDate dataVencimento,
        StatusTarefa status) {

    public static TarefaResponse fromEntity(Tarefa tarefa) {
        return new TarefaResponse(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getDataVencimento(),
                tarefa.getStatus()
        );
    }
}
