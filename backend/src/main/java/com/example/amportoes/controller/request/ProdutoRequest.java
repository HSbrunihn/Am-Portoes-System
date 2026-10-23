
package com.example.amportoes.controller.request;

import com.example.amportoes.entity.Produto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotNull(message = "O código é obrigatório")
        Long codigo,

        String descricao,

        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        BigDecimal preco,

        @PositiveOrZero(
                message = "A quantidade em estoque não pode ser negativa")
        Integer quantidadeEstoque,

        @PositiveOrZero(
                message = "O estoque mínimo não pode ser negativo")
        Integer estoqueMinimo,

        LocalDate dataValidade
) {
    public Produto toEntity() {
        Produto produto = new Produto();
        produto.setNome(this.nome());
        produto.setCodigo(this.codigo());
        produto.setDescricao(this.descricao());
        produto.setPreco(this.preco());
        produto.setQuantidadeEstoque(this.quantidadeEstoque());
        produto.setEstoqueMinimo(this.estoqueMinimo());
        produto.setDataValidade(this.dataValidade());
        return produto;
    }
}
