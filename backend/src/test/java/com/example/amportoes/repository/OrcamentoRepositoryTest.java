package com.example.amportoes.repository;

import com.example.amportoes.entity.Orcamento;
import com.example.amportoes.entity.StatusTarefa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrcamentoRepositoryTest {

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @Test
    void givenOrcamentosComStatusDistintosQuandoBuscaPorStatusEntaoRetornaSomenteStatusSolicitado() {
        // Given
        orcamentoRepository.save(new Orcamento(null, "Reforma", "", null, null, StatusTarefa.PENDENTE));
        orcamentoRepository.save(new Orcamento(null, "InstalaÃ§Ã£o", "", null, null, StatusTarefa.PENDENTE));
        orcamentoRepository.save(new Orcamento(null, "ManutenÃ§Ã£o", "", null, null, StatusTarefa.CONCLUIDA));

        // When
        List<Orcamento> resultado = orcamentoRepository.findByStatus(StatusTarefa.PENDENTE);

        // Then
        assertThat(resultado).hasSize(2)
                .allSatisfy(orcamento -> assertThat(orcamento.getStatus()).isEqualTo(StatusTarefa.PENDENTE));
    }
}


