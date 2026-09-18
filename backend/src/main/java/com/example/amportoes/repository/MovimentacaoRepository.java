package com.example.amportoes.repository;

import com.example.amportoes.entity.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoRepository extends JpaRepository<MovimentacaoEstoque, Long> {
}
