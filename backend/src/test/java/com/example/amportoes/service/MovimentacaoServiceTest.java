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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {

    @Mock
    MovimentacaoRepository movimentacaoRepository;

    @Mock
    ProdutoRepository produtoRepository;

    @InjectMocks
    MovimentacaoService movimentacaoService;

    @Test
    void givenProdutoComEstoqueQuandoRegistraEntradaEntaoAumentaEstoqueEPersisteMovimentacao() {
        Produto produto = produto(4L, 5);

        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));
        when(movimentacaoRepository.save(any(MovimentacaoEstoque.class)))
                .thenAnswer(call -> {
                    MovimentacaoEstoque salva = call.getArgument(0);
                    salva.setId(9L);
                    return salva;
                });

        MovimentacaoResponseDTO resultado =
                movimentacaoService.registrarMovimentacao(
                        dto(4L, 3, TipoMovimentacao.ENTRADA)
                );

        assertEquals(8, produto.getQuantidadeEstoque());
        assertEquals(9L, resultado.id());
        assertEquals("Produto", resultado.produtoNome());

        verify(produtoRepository).save(produto);
        verify(movimentacaoRepository).save(any(MovimentacaoEstoque.class));
    }

    @Test
    void givenEstoqueInsuficienteQuandoRegistraSaidaEntaoLancaRegraNegocioESemPersistir() {
        Produto produto = produto(4L, 2);

        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));

        RegraNegocioException excecao = assertThrows(
                RegraNegocioException.class,
                () -> movimentacaoService.registrarMovimentacao(
                        dto(4L, 5, TipoMovimentacao.SAIDA)
                )
        );

        assertNotNull(excecao);
        assertEquals(2, produto.getQuantidadeEstoque());

        verify(produtoRepository, never()).save(any(Produto.class));
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    void givenProdutoInexistenteQuandoRegistraMovimentacaoEntaoLancaRecursoNaoEncontrado() {
        when(produtoRepository.findById(44L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> movimentacaoService.registrarMovimentacao(
                        dto(44L, 1, TipoMovimentacao.ENTRADA)
                )
        );

        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    void givenProdutoInexistenteQuandoListaMovimentacoesPorProdutoEntaoLancaRecursoNaoEncontrado() {
        when(produtoRepository.existsById(44L)).thenReturn(false);

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> movimentacaoService.listarPorProduto(44L)
        );

        verify(produtoRepository).existsById(44L);
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    void givenEstoqueNuloQuandoRegistraEntradaEntaoIniciaEstoqueEmZero() {
        Produto produto = produto(4L, null);

        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));
        when(movimentacaoRepository.save(any(MovimentacaoEstoque.class)))
                .thenAnswer(call -> {
                    MovimentacaoEstoque movimentacao = call.getArgument(0);
                    movimentacao.setId(10L);
                    return movimentacao;
                });

        MovimentacaoResponseDTO resultado =
                movimentacaoService.registrarMovimentacao(
                        dto(4L, 3, TipoMovimentacao.ENTRADA)
                );

        assertEquals(3, produto.getQuantidadeEstoque());
        assertEquals(10L, resultado.id());

        verify(produtoRepository).save(produto);
        verify(movimentacaoRepository).save(any(MovimentacaoEstoque.class));
    }

    @Test
    void givenEstoqueNuloQuandoRegistraSaidaEntaoLancaExcecao() {
        Produto produto = produto(4L, null);

        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));

        assertThrows(
                RegraNegocioException.class,
                () -> movimentacaoService.registrarMovimentacao(
                        dto(4L, 1, TipoMovimentacao.SAIDA)
                )
        );

        verify(produtoRepository, never()).save(any(Produto.class));
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    void givenEstoqueSuficienteQuandoRegistraSaidaEntaoSubtraiEstoque() {
        Produto produto = produto(4L, 10);

        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));
        when(movimentacaoRepository.save(any(MovimentacaoEstoque.class)))
                .thenAnswer(call -> {
                    MovimentacaoEstoque movimentacao = call.getArgument(0);
                    movimentacao.setId(11L);
                    return movimentacao;
                });

        MovimentacaoResponseDTO resultado =
                movimentacaoService.registrarMovimentacao(
                        dto(4L, 4, TipoMovimentacao.SAIDA)
                );

        assertEquals(6, produto.getQuantidadeEstoque());
        assertEquals(TipoMovimentacao.SAIDA, resultado.tipo());

        verify(produtoRepository).save(produto);
        verify(movimentacaoRepository).save(any(MovimentacaoEstoque.class));
    }

    @Test
    void givenTipoNuloQuandoRegistraMovimentacaoEntaoLancaExcecao() {
        Produto produto = produto(4L, 10);

        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));

        assertThrows(
                RegraNegocioException.class,
                () -> movimentacaoService.registrarMovimentacao(
                        dto(4L, 1, null)
                )
        );

        verify(produtoRepository, never()).save(any(Produto.class));
        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    void givenDataInformadaQuandoRegistraMovimentacaoEntaoMantemData() {
        Produto produto = produto(4L, 5);
        LocalDateTime dataInformada = LocalDateTime.of(2026, 10, 20, 14, 30);

        MovimentacaoDTO movimentacaoDTO =
                dto(4L, 2, TipoMovimentacao.ENTRADA);
        movimentacaoDTO.setDataMovimentacao(dataInformada);

        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));
        when(movimentacaoRepository.save(any(MovimentacaoEstoque.class)))
                .thenAnswer(call -> {
                    MovimentacaoEstoque movimentacao = call.getArgument(0);
                    movimentacao.setId(12L);
                    return movimentacao;
                });

        MovimentacaoResponseDTO resultado =
                movimentacaoService.registrarMovimentacao(movimentacaoDTO);

        assertEquals(dataInformada, resultado.dataMovimentacao());
    }

    @Test
    void givenMovimentacoesExistentesQuandoListaTodasEntaoRetornaDtos() {
        Produto produto = produto(4L, 8);
        MovimentacaoEstoque movimentacao = movimentacao(
                20L,
                produto,
                3,
                TipoMovimentacao.ENTRADA,
                LocalDateTime.of(2026, 10, 20, 10, 0)
        );

        when(movimentacaoRepository.findAll()).thenReturn(List.of(movimentacao));

        List<MovimentacaoResponseDTO> resultado =
                movimentacaoService.listarMovimentacoes();

        assertEquals(1, resultado.size());
        assertEquals(20L, resultado.get(0).id());
        assertEquals(4L, resultado.get(0).produtoId());
        assertEquals("Produto", resultado.get(0).produtoNome());
        assertEquals(3, resultado.get(0).quantidade());
        assertEquals(TipoMovimentacao.ENTRADA, resultado.get(0).tipo());
    }

    @Test
    void givenNenhumaMovimentacaoQuandoListaTodasEntaoRetornaListaVazia() {
        when(movimentacaoRepository.findAll()).thenReturn(List.of());

        List<MovimentacaoResponseDTO> resultado =
                movimentacaoService.listarMovimentacoes();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void givenProdutoExistenteComMovimentacoesQuandoListaPorProdutoEntaoRetornaDtos() {
        Produto produto = produto(4L, 8);

        MovimentacaoEstoque movimentacao = movimentacao(
                30L,
                produto,
                3,
                TipoMovimentacao.ENTRADA,
                LocalDateTime.of(2026, 10, 20, 10, 0)
        );

        when(produtoRepository.existsById(4L)).thenReturn(true);
        when(movimentacaoRepository.findByProdutoIdOrderByDataMovimentacaoDesc(4L))
                .thenReturn(List.of(movimentacao));

        List<MovimentacaoResponseDTO> resultado =
                movimentacaoService.listarPorProduto(4L);

        assertEquals(1, resultado.size());
        assertEquals(30L, resultado.get(0).id());
        assertEquals(4L, resultado.get(0).produtoId());
        assertEquals("Produto", resultado.get(0).produtoNome());
        assertEquals(3, resultado.get(0).quantidade());

        verify(produtoRepository).existsById(4L);
        verify(movimentacaoRepository)
                .findByProdutoIdOrderByDataMovimentacaoDesc(4L);
    }

    @Test
    void givenProdutoExistenteSemMovimentacoesQuandoListaPorProdutoEntaoRetornaListaVazia() {
        when(produtoRepository.existsById(4L)).thenReturn(true);
        when(movimentacaoRepository.findByProdutoIdOrderByDataMovimentacaoDesc(4L))
                .thenReturn(List.of());

        List<MovimentacaoResponseDTO> resultado =
                movimentacaoService.listarPorProduto(4L);

        assertTrue(resultado.isEmpty());

        verify(produtoRepository).existsById(4L);
        verify(movimentacaoRepository)
                .findByProdutoIdOrderByDataMovimentacaoDesc(4L);
    }

    private Produto produto(Long id, Integer estoque) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome("Produto");
        produto.setQuantidadeEstoque(estoque);
        return produto;
    }

    private MovimentacaoDTO dto(
            Long produtoId,
            int quantidade,
            TipoMovimentacao tipo) {

        MovimentacaoDTO dto = new MovimentacaoDTO();
        dto.setProdutoId(produtoId);
        dto.setQuantidade(quantidade);
        dto.setTipo(tipo);
        return dto;
    }

    private MovimentacaoEstoque movimentacao(
            Long id,
            Produto produto,
            int quantidade,
            TipoMovimentacao tipo,
            LocalDateTime data) {

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setId(id);
        movimentacao.setProduto(produto);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setTipo(tipo);
        movimentacao.setDataMovimentacao(data);
        return movimentacao;
    }
}