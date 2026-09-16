package com.example.amportoes.controller.response;

import com.example.amportoes.entity.Orcamento;
import com.example.amportoes.entity.StatusTarefa;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrcamentoResponse(
        Long id,
        String titulo,
        String descricao,
        BigDecimal valor,
        LocalDate dataVencimento,
        StatusTarefa status
) {
    public static OrcamentoResponse fromEntity(Orcamento orcamento) {
        return new OrcamentoResponse(
                orcamento.getId(),
                orcamento.getTitulo(),
                orcamento.getDescricao(),
                orcamento.getValor(),
                orcamento.getDataVencimento(),
                orcamento.getStatus()
        );
    }
}
