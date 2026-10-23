package com.example.amportoes.service;

import com.example.amportoes.controller.request.OrcamentoRequest;
import com.example.amportoes.controller.response.OrcamentoResponse;
import com.example.amportoes.entity.Orcamento;
import com.example.amportoes.entity.StatusTarefa;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.repository.OrcamentoRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrcamentoServiceTest {
    @Mock OrcamentoRepository orcamentoRepository;
    @InjectMocks OrcamentoService orcamentoService;

    @Test
    void givenDadosValidosQuandoCriaOrcamentoEntaoSalvaComStatusPendente() {
        // Given
        OrcamentoRequest request = new OrcamentoRequest("Instalacao", "Grade", new BigDecimal("1500.00"), LocalDate.of(2026, 12, 20));
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(call -> call.getArgument(0));

        // When
        OrcamentoResponse resultado = orcamentoService.criar(request);

        // Then
        assertEquals("Instalacao", resultado.titulo());
        assertEquals(StatusTarefa.PENDENTE, resultado.status());
        verify(orcamentoRepository).save(argThat(o -> o.getStatus() == StatusTarefa.PENDENTE));
    }

    @Test
    void givenStatusInformadoQuandoListaOrcamentosEntaoBuscaPeloStatusSolicitado() {
        // Given
        Orcamento orcamento = orcamento(3L, "Reforma", StatusTarefa.EM_ANDAMENTO);
        when(orcamentoRepository.findByStatus(StatusTarefa.EM_ANDAMENTO)).thenReturn(List.of(orcamento));

        // When
        List<OrcamentoResponse> resultado = orcamentoService.listar(StatusTarefa.EM_ANDAMENTO);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(StatusTarefa.EM_ANDAMENTO, resultado.get(0).status());
        verify(orcamentoRepository).findByStatus(StatusTarefa.EM_ANDAMENTO);
        verify(orcamentoRepository, never()).findAll();
    }

    @Test
    void givenOrcamentoInexistenteQuandoBuscaPorIdEntaoLancaRecursoNaoEncontrado() {
        // Given
        when(orcamentoRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class, () -> orcamentoService.buscarPorId(99L));

        // Then
        assertNotNull(excecao);
    }

    @Test
    void givenOrcamentoInexistenteQuandoDeletaEntaoLancaExcecaoESemApagar() {
        // Given
        when(orcamentoRepository.existsById(99L)).thenReturn(false);

        // When
        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class, () -> orcamentoService.deletar(99L));

        // Then
        assertNotNull(excecao);
        verify(orcamentoRepository, never()).deleteById(anyLong());
    }

    @Test
    void givenOrcamentoExistenteQuandoAtualizaDadosEntaoSalvaValoresAtualizados() {
        // Given
        Orcamento existente = orcamento(6L, "Reforma", StatusTarefa.PENDENTE);
        OrcamentoRequest request = new OrcamentoRequest("Reforma nova", "Descricao", new BigDecimal("2000.00"), LocalDate.of(2027, 1, 10));
        when(orcamentoRepository.findById(6L)).thenReturn(Optional.of(existente));
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(call -> call.getArgument(0));

        // When
        OrcamentoResponse resultado = orcamentoService.atualizar(6L, request);

        // Then
        assertEquals("Reforma nova", resultado.titulo());
        assertEquals(new BigDecimal("2000.00"), resultado.valor());
        verify(orcamentoRepository).save(existente);
    }

    private Orcamento orcamento(Long id, String titulo, StatusTarefa status) {
        return new Orcamento(id, titulo, "", BigDecimal.ONE, LocalDate.of(2026, 12, 20), status);
    }
}