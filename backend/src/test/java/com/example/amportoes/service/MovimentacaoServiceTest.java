package com.example.amportoes.service;

import com.example.amportoes.dto.MovimentacaoDTO;
import com.example.amportoes.dto.MovimentacaoResponseDTO;
import com.example.amportoes.entity.MovimentacaoEstoque;
import com.example.amportoes.entity.Produto;
import com.example.amportoes.entity.TipoMovimentacao;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.exception.RegraNegocioException;
import com.example.amportoes.repository.MovimentacaoRepository;
import com.example.amportoes.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {
    @Mock MovimentacaoRepository movimentacaoRepository;
    @Mock ProdutoRepository produtoRepository;
    @InjectMocks MovimentacaoService movimentacaoService;

    @Test
    void givenProdutoComEstoqueQuandoRegistraEntradaEntaoAumentaEstoqueEPersisteMovimentacao() {
        // Given
        Produto produto = produto(4L, 5);
        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));
        when(movimentacaoRepository.save(any(MovimentacaoEstoque.class))).thenAnswer(call -> {
            MovimentacaoEstoque salva = call.getArgument(0);
            salva.setId(9L);
            return salva;
        });
        MovimentacaoDTO dto = dto(4L, 3, TipoMovimentacao.ENTRADA);

        // When
        MovimentacaoResponseDTO resultado = movimentacaoService.registrarMovimentacao(dto);

        // Then
        assertEquals(8, produto.getQuantidadeEstoque());
        assertEquals(9L, resultado.id());
        assertEquals("Produto", resultado.produtoNome());
        verify(produtoRepository).save(produto);
        verify(movimentacaoRepository).save(any(MovimentacaoEstoque.class));
    }

    @Test
    void givenEstoqueInsuficienteQuandoRegistraSaidaEntaoLancaRegraNegocioESemPersistir() {
        // Given
        Produto produto = produto(4L, 2);
        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));
        MovimentacaoDTO dto = dto(4L, 5, TipoMovimentacao.SAIDA);

        // When
        RegraNegocioException excecao = assertThrows(RegraNegocioException.class, () -> movimentacaoService.registrarMovimentacao(dto));

        // Then
        assertNotNull(excecao);
        assertEquals(2, produto.getQuantidadeEstoque());
        verify(produtoRepository, never()).save(any(Produto.class));
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    void givenProdutoInexistenteQuandoRegistraMovimentacaoEntaoLancaRecursoNaoEncontrado() {
        // Given
        when(produtoRepository.findById(44L)).thenReturn(Optional.empty());

        // When
        assertThrows(RecursoNaoEncontradoException.class,
                () -> movimentacaoService.registrarMovimentacao(dto(44L, 1, TipoMovimentacao.ENTRADA)));
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    void givenProdutoInexistenteQuandoListaMovimentacoesPorProdutoEntaoLancaRecursoNaoEncontrado() {
        // Given
        when(produtoRepository.existsById(44L)).thenReturn(false);

        // When
        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class, () -> movimentacaoService.listarPorProduto(44L));

        // Then
        assertNotNull(excecao);
        verifyNoInteractions(movimentacaoRepository);
    }

    private Produto produto(Long id, int estoque) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome("Produto");
        produto.setQuantidadeEstoque(estoque);
        return produto;
    }

    private MovimentacaoDTO dto(Long produtoId, int quantidade, TipoMovimentacao tipo) {
        MovimentacaoDTO dto = new MovimentacaoDTO();
        dto.setProdutoId(produtoId);
        dto.setQuantidade(quantidade);
        dto.setTipo(tipo);
        return dto;
    }
}