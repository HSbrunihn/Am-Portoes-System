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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    TarefaRepository tarefaRepository;

    @InjectMocks
    TarefaService tarefaService;

    @Test
    void givenDadosValidosQuandoCriaTarefaEntaoSalvaComStatusPendente() {
        TarefaRequest request = new TarefaRequest(
                "Instalar portao", "", null,
                LocalDate.of(2026, 12, 1)
        );

        when(tarefaRepository.save(any(Tarefa.class)))
                .thenAnswer(call -> call.getArgument(0));

        TarefaResponse resultado = tarefaService.criar(request);

        assertEquals("Instalar portao", resultado.titulo());
        assertEquals(StatusTarefa.PENDENTE, resultado.status());

        verify(tarefaRepository).save(argThat(
                tarefa -> tarefa.getStatus() == StatusTarefa.PENDENTE
        ));
    }

    @Test
    void givenStatusEspecificoQuandoListaTarefasEntaoConsultaSomenteEsseStatus() {
        Tarefa tarefa = tarefa(4L, "Conferir", StatusTarefa.CONCLUIDA);

        when(tarefaRepository.findByStatus(StatusTarefa.CONCLUIDA))
                .thenReturn(List.of(tarefa));

        List<TarefaResponse> resultado =
                tarefaService.listar(StatusTarefa.CONCLUIDA);

        assertEquals(1, resultado.size());
        assertEquals(StatusTarefa.CONCLUIDA, resultado.get(0).status());

        verify(tarefaRepository).findByStatus(StatusTarefa.CONCLUIDA);
        verify(tarefaRepository, never()).findAll();
    }

    @Test
    void givenStatusNuloQuandoListaTarefasEntaoConsultaTodas() {
        when(tarefaRepository.findAll())
                .thenReturn(List.of(
                        tarefa(1L, "Instalar portao", StatusTarefa.PENDENTE),
                        tarefa(2L, "Conferir estoque", StatusTarefa.EM_ANDAMENTO)
                ));

        List<TarefaResponse> resultado = tarefaService.listar(null);

        assertEquals(2, resultado.size());
        assertEquals("Instalar portao", resultado.get(0).titulo());
        assertEquals("Conferir estoque", resultado.get(1).titulo());

        verify(tarefaRepository).findAll();
        verify(tarefaRepository, never()).findByStatus(any());
    }

    @Test
    void givenNenhumaTarefaQuandoListaEntaoRetornaListaVazia() {
        when(tarefaRepository.findAll()).thenReturn(List.of());

        List<TarefaResponse> resultado = tarefaService.listar(null);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(tarefaRepository).findAll();
    }

    @Test
    void givenIdExistenteQuandoBuscaEntaoRetornaTarefa() {
        when(tarefaRepository.findById(4L))
                .thenReturn(Optional.of(
                        tarefa(4L, "Conferir", StatusTarefa.PENDENTE)
                ));

        TarefaResponse resultado = tarefaService.buscarPorId(4L);

        assertEquals(4L, resultado.id());
        assertEquals("Conferir", resultado.titulo());
        assertEquals(StatusTarefa.PENDENTE, resultado.status());

        verify(tarefaRepository).findById(4L);
    }

    @Test
    void givenIdSemTarefaQuandoBuscaEntaoLancaRecursoNaoEncontrado() {
        when(tarefaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> tarefaService.buscarPorId(99L)
        );

        verify(tarefaRepository).findById(99L);
    }

    @Test
    void givenDadosValidosQuandoAtualizaTarefaEntaoSalvaAlteracoes() {
        Tarefa existente = tarefa(
                4L, "Titulo antigo", StatusTarefa.PENDENTE
        );

        TarefaRequest request = new TarefaRequest(
                "Titulo novo",
                "Descricao nova",
                new BigDecimal("150.00"),
                LocalDate.of(2027, 1, 15)
        );

        when(tarefaRepository.findById(4L))
                .thenReturn(Optional.of(existente));

        when(tarefaRepository.save(any(Tarefa.class)))
                .thenAnswer(call -> call.getArgument(0));

        TarefaResponse resultado = tarefaService.atualizar(4L, request);

        assertEquals("Titulo novo", resultado.titulo());
        assertEquals("Descricao nova", resultado.descricao());
        assertEquals(new BigDecimal("150.00"), resultado.valor());
        assertEquals(LocalDate.of(2027, 1, 15), resultado.dataVencimento());

        verify(tarefaRepository).findById(4L);
        verify(tarefaRepository).save(existente);
    }

    @Test
    void givenIdInexistenteQuandoAtualizaTarefaEntaoLancaExcecao() {
        when(tarefaRepository.findById(99L))
                .thenReturn(Optional.empty());

        TarefaRequest request = new TarefaRequest(
                "Titulo", "Descricao", BigDecimal.TEN,
                LocalDate.of(2027, 1, 15)
        );

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> tarefaService.atualizar(99L, request)
        );

        verify(tarefaRepository, never()).save(any(Tarefa.class));
    }

    @Test
    void givenTarefaExistenteQuandoAtualizaStatusEntaoPersisteNovoStatus() {
        Tarefa existente = tarefa(
                4L, "Conferir", StatusTarefa.PENDENTE
        );

        when(tarefaRepository.findById(4L))
                .thenReturn(Optional.of(existente));

        when(tarefaRepository.save(any(Tarefa.class)))
                .thenAnswer(call -> call.getArgument(0));

        TarefaResponse resultado =
                tarefaService.atualizarStatus(4L, StatusTarefa.EM_ANDAMENTO);

        assertEquals(StatusTarefa.EM_ANDAMENTO, resultado.status());
        verify(tarefaRepository).save(existente);
    }

    @Test
    void givenIdInexistenteQuandoAtualizaStatusEntaoLancaExcecao() {
        when(tarefaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> tarefaService.atualizarStatus(
                        99L, StatusTarefa.CONCLUIDA
                )
        );

        verify(tarefaRepository, never()).save(any(Tarefa.class));
    }

    @Test
    void givenIdSemTarefaQuandoDeletaEntaoLancaExcecaoESemChamarExclusao() {
        when(tarefaRepository.existsById(88L)).thenReturn(false);

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> tarefaService.deletar(88L)
        );

        verify(tarefaRepository).existsById(88L);
        verify(tarefaRepository, never()).deleteById(anyLong());
    }

    @Test
    void givenTarefaExistenteQuandoDeletaEntaoRemoveDoRepositorio() {
        when(tarefaRepository.existsById(4L)).thenReturn(true);

        assertDoesNotThrow(() -> tarefaService.deletar(4L));

        verify(tarefaRepository).existsById(4L);
        verify(tarefaRepository).deleteById(4L);
    }

    private Tarefa tarefa(Long id, String titulo, StatusTarefa status) {
        return new Tarefa(
                id,
                status,
                titulo,
                "",
                null,
                LocalDate.of(2026, 12, 1)
        );
    }
}