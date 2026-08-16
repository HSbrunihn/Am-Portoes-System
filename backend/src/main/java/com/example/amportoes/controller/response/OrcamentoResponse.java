package com.example.amportoes.controller.response;

import com.example.amportoes.entity.Orcamento;
import com.example.amportoes.entity.StatusTarefa;

import java.time.LocalDate;

public record OrcamentoResponse(
        Long id,
        String titulo,
        String descricao,
        LocalDate dataVencimento,
        StatusTarefa status
) {
    public static OrcamentoResponse fromEntity(Orcamento orcamento) {
        return new OrcamentoResponse(
                orcamento.getId(),
                orcamento.getTitulo(),
                orcamento.getDescricao(),
                orcamento.getDataVencimento(),
                orcamento.getStatus()
        );
    }
}
