
package com.example.amportoes.controller.response;

import com.example.amportoes.entity.Produto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoResponse(
        Long id,
        Long codigo,
        String nome,
        BigDecimal preco,
        Integer quantidadeEstoque,
        Integer estoqueMinimo,
        LocalDate dataValidade
) {
    public static ProdutoResponse fromEntity(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getCodigo(),
                produto.getNome(),
                produto.getPreco(),
                produto.getQuantidadeEstoque(),
                produto.getEstoqueMinimo(),
                produto.getDataValidade()
        );
    }
}
