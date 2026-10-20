
package com.example.amportoes.repository;

import com.example.amportoes.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByNomeContainingIgnoreCase(String nome);

    @Query("""
        SELECT p FROM Produto p
        WHERE p.quantidadeEstoque IS NOT NULL
          AND p.estoqueMinimo IS NOT NULL
          AND p.quantidadeEstoque <= p.estoqueMinimo
        """)
    List<Produto> findProdutosComBaixoEstoque();
}
