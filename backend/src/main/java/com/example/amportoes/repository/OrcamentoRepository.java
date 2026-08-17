package com.example.amportoes.repository;

import com.example.amportoes.entity.Orcamento;
import com.example.amportoes.entity.StatusTarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    List<Orcamento> findByStatus(StatusTarefa status);
}