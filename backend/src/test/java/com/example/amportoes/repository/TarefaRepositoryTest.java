package com.example.amportoes.repository;

import com.example.amportoes.entity.StatusTarefa;
import com.example.amportoes.entity.Tarefa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TarefaRepositoryTest {

    @Autowired
    private TarefaRepository tarefaRepository;

    @Test
    void givenTarefasComStatusDistintosQuandoBuscaPorStatusEntaoRetornaSomenteStatusSolicitado() {
        // Given
        tarefaRepository.save(new Tarefa(null, StatusTarefa.PENDENTE, "Revisar estoque", "", null, null));
        tarefaRepository.save(new Tarefa(null, StatusTarefa.PENDENTE, "Separar pedidos", "", null, null));
        tarefaRepository.save(new Tarefa(null, StatusTarefa.CONCLUIDA, "Conferir entrega", "", null, null));

        // When
        List<Tarefa> resultado = tarefaRepository.findByStatus(StatusTarefa.PENDENTE);

        // Then
        assertThat(resultado).hasSize(2)
                .allSatisfy(tarefa -> assertThat(tarefa.getStatus()).isEqualTo(StatusTarefa.PENDENTE));
    }
}
