package com.example.amportoes.service;

import com.example.amportoes.controller.request.TarefaRequest;
import com.example.amportoes.controller.response.TarefaResponse;
import com.example.amportoes.entity.StatusTarefa;
import com.example.amportoes.entity.Tarefa;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.repository.TarefaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {
    @Mock TarefaRepository tarefaRepository;
    @InjectMocks TarefaService tarefaService;

    @Test
    void givenDadosValidosQuandoCriaTarefaEntaoSalvaComStatusPendente() {
        // Given
        TarefaRequest request = new TarefaRequest("Instalar portao", "", null, LocalDate.of(2026, 12, 1));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(call -> call.getArgument(0));

        // When
        TarefaResponse resultado = tarefaService.criar(request);

        // Then
        assertEquals("Instalar portao", resultado.titulo());
        assertEquals(StatusTarefa.PENDENTE, resultado.status());
        verify(tarefaRepository).save(argThat(t -> t.getStatus() == StatusTarefa.PENDENTE));
    }

    @Test
    void givenStatusEspecificoQuandoListaTarefasEntaoConsultaSomenteEsseStatus() {
        // Given
        Tarefa tarefa = tarefa(4L, "Conferir", StatusTarefa.CONCLUIDA);
        when(tarefaRepository.findByStatus(StatusTarefa.CONCLUIDA)).thenReturn(List.of(tarefa));

        // When
        List<TarefaResponse> resultado = tarefaService.listar(StatusTarefa.CONCLUIDA);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(StatusTarefa.CONCLUIDA, resultado.get(0).status());
        verify(tarefaRepository).findByStatus(StatusTarefa.CONCLUIDA);
        verify(tarefaRepository, never()).findAll();
    }

    @Test
    void givenIdSemTarefaQuandoBuscaEntaoLancaRecursoNaoEncontrado() {
        // Given
        when(tarefaRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class, () -> tarefaService.buscarPorId(99L));

        // Then
        assertNotNull(excecao);
    }

    @Test
    void givenTarefaExistenteQuandoAtualizaStatusEntaoPersisteNovoStatus() {
        // Given
        Tarefa existente = tarefa(4L, "Conferir", StatusTarefa.PENDENTE);
        when(tarefaRepository.findById(4L)).thenReturn(Optional.of(existente));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(call -> call.getArgument(0));

        // When
        TarefaResponse resultado = tarefaService.atualizarStatus(4L, StatusTarefa.EM_ANDAMENTO);

        // Then
        assertEquals(StatusTarefa.EM_ANDAMENTO, resultado.status());
        verify(tarefaRepository).save(existente);
    }

    @Test
    void givenIdSemTarefaQuandoDeletaEntaoLancaExcecaoESemChamarExclusao() {
        // Given
        when(tarefaRepository.existsById(88L)).thenReturn(false);

        // When
        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class, () -> tarefaService.deletar(88L));

        // Then
        assertNotNull(excecao);
        verify(tarefaRepository, never()).deleteById(anyLong());
    }

    private Tarefa tarefa(Long id, String titulo, StatusTarefa status) {
        return new Tarefa(id, status, titulo, "", null, LocalDate.of(2026, 12, 1));
    }
}