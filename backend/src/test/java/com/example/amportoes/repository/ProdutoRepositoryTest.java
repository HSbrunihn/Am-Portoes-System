package com.example.amportoes.repository;

import com.example.amportoes.entity.Produto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProdutoRepositoryTest {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    void givenProdutosComNomeSemelhanteQuandoBuscaPorTrechoIgnorandoCaixaEntaoRetornaCorrespondencias() {
        // Given
        produtoRepository.save(produto("Portão basculante", 5, 2));
        produtoRepository.save(produto("PORTÃO social", 3, 1));
        produtoRepository.save(produto("Motor", 8, 2));

        // When
        List<Produto> resultado = produtoRepository.findByNomeContainingIgnoreCase("portão");

        // Then
        assertThat(resultado).extracting(Produto::getNome)
                .containsExactlyInAnyOrder("Portão basculante", "PORTÃO social");
    }

    @Test
    void givenProdutosDentroEAbaixoDoMinimoQuandoBuscaBaixoEstoqueEntaoRetornaSomenteEstoqueAbaixoOuIgualAoMinimo() {
        // Given
        produtoRepository.save(produto("Abaixo", 1, 2));
        produtoRepository.save(produto("No limite", 2, 2));
        produtoRepository.save(produto("Acima", 3, 2));

        // When
        List<Produto> resultado = produtoRepository.findProdutosComBaixoEstoque();

        // Then
        assertThat(resultado).extracting(Produto::getNome)
                .containsExactlyInAnyOrder("Abaixo", "No limite");
    }

    private Produto produto(String nome, int quantidade, int minimo) {
        return new Produto(null, null, nome, "", BigDecimal.ONE, quantidade, minimo, null);
    }
}
