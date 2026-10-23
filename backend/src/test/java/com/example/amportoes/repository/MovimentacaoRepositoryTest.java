package com.example.amportoes.repository;

import com.example.amportoes.entity.MovimentacaoEstoque;
import com.example.amportoes.entity.Produto;
import com.example.amportoes.entity.TipoMovimentacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MovimentacaoRepositoryTest {

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    void givenMovimentacoesDeProdutosEDatasDiferentesQuandoBuscaPorProdutoEntaoRetornaOrdenadasDaMaisRecente() {
        // Given
        Produto produtoAlvo = produtoRepository.save(new Produto(null, 1L, "PortÃ£o", "", null, 10, 2, null));
        Produto outroProduto = produtoRepository.save(new Produto(null, 2L, "Motor", "", null, 5, 1, null));
        MovimentacaoEstoque antiga = movimentacao(produtoAlvo, LocalDateTime.of(2026, 1, 1, 10, 0));
        MovimentacaoEstoque recente = movimentacao(produtoAlvo, LocalDateTime.of(2026, 1, 3, 10, 0));
        movimentacaoRepository.save(antiga);
        movimentacaoRepository.save(recente);
        movimentacaoRepository.save(movimentacao(outroProduto, LocalDateTime.of(2026, 1, 4, 10, 0)));

        // When
        List<MovimentacaoEstoque> resultado = movimentacaoRepository
                .findByProdutoIdOrderByDataMovimentacaoDesc(produtoAlvo.getId());

        // Then
        assertThat(resultado).containsExactly(recente, antiga);
    }

    private MovimentacaoEstoque movimentacao(Produto produto, LocalDateTime data) {
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setProduto(produto);
        movimentacao.setQuantidade(1);
        movimentacao.setTipo(TipoMovimentacao.ENTRADA);
        movimentacao.setDataMovimentacao(data);
        return movimentacao;
    }
}


