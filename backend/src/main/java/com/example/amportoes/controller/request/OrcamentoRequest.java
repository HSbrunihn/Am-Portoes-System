package com.example.amportoes.controller.request;

import com.example.amportoes.entity.Orcamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrcamentoRequest(
        @NotBlank(message = "O título é obrigatório") String titulo,
        String descricao,
        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.0", message = "O valor não pode ser negativo") BigDecimal valor,
        @NotNull(message = "A data de vencimento é obrigatória") LocalDate dataVencimento
) {
    public Orcamento toEntity() {
        Orcamento orcamento = new Orcamento();
        orcamento.setTitulo(this.titulo());
        orcamento.setDescricao(this.descricao());
        orcamento.setValor(this.valor());
        orcamento.setDataVencimento(this.dataVencimento());
        return orcamento;
    }
}
