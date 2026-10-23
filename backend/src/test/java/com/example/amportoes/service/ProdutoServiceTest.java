package com.example.amportoes.service;

import com.example.amportoes.controller.request.ProdutoRequest;
import com.example.amportoes.controller.response.ProdutoResponse;
import com.example.amportoes.dto.ProdutoDTO;
import com.example.amportoes.entity.Produto;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    ProdutoRepository produtoRepository;

    @InjectMocks
    ProdutoService produtoService;

    @Test
    void givenDadosDoDtoQuandoCriaProdutoEntaoPersisteEDevolveDadosConvertidos() {
        ProdutoDTO entrada = new ProdutoDTO(
                "Sensor", "Teste", new BigDecimal("25.90"), 10, 2
        );

        when(produtoRepository.save(any(Produto.class)))
                .thenAnswer(call -> call.getArgument(0));

        ProdutoDTO resultado = produtoService.criar(entrada);

        assertEquals("Sensor", resultado.getNome());
        assertEquals(new BigDecimal("25.90"), resultado.getPreco());
        assertEquals(10, resultado.getQuantidadeEstoque());

        verify(produtoRepository)
                .save(argThat(p -> p.getQuantidadeEstoque() == 10));
    }

    @Test
    void givenRequisicaoValidaQuandoCriaProdutoEntaoRetornaResponseDoProdutoSalvo() {
        ProdutoRequest request = new ProdutoRequest(
                "Portao", 7L, "Basculante",
                new BigDecimal("100.00"), 4, 1, null
        );

        when(produtoRepository.save(any(Produto.class))).thenAnswer(call -> {
            Produto salvo = call.getArgument(0);
            salvo.setId(3L);
            return salvo;
        });

        ProdutoResponse resultado = produtoService.criar(request);

        assertEquals(3L, resultado.id());
        assertEquals("Portao", resultado.nome());
        assertEquals(7L, resultado.codigo());
    }

    @Test
    void givenProdutosQuandoListaDtoEntaoRetornaTodosConvertidos() {
        when(produtoRepository.findAll())
                .thenReturn(List.of(
                        produto(1L, "Motor"),
                        produto(2L, "Sensor")
                ));

        List<ProdutoDTO> resultado = produtoService.listar();

        assertEquals(2, resultado.size());
        assertEquals("Motor", resultado.get(0).getNome());
        assertEquals("Sensor", resultado.get(1).getNome());

        verify(produtoRepository).findAll();
    }

    @Test
    void givenListaVaziaQuandoListaDtoEntaoRetornaListaVazia() {
        when(produtoRepository.findAll()).thenReturn(List.of());

        List<ProdutoDTO> resultado = produtoService.listar();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void givenNomeDeBuscaQuandoListaProdutosEntaoUsaBuscaParcialIgnorandoCaixa() {
        when(produtoRepository.findByNomeContainingIgnoreCase("mot"))
                .thenReturn(List.of(produto(1L, "Motor")));

        List<ProdutoResponse> resultado = produtoService.listar("mot");

        assertEquals(1, resultado.size());
        assertEquals("Motor", resultado.get(0).nome());

        verify(produtoRepository).findByNomeContainingIgnoreCase("mot");
        verify(produtoRepository, never()).findAll();
    }

    @Test
    void givenListaSemNomeQuandoListaResponsesEntaoConsultaTodos() {
        when(produtoRepository.findAll())
                .thenReturn(List.of(produto(1L, "Motor")));

        List<ProdutoResponse> resultado = produtoService.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("Motor", resultado.get(0).nome());

        verify(produtoRepository).findAll();
        verify(produtoRepository, never())
                .findByNomeContainingIgnoreCase(anyString());
    }

    @Test
    void givenNomeEmBrancoQuandoListaResponsesEntaoConsultaTodos() {
        when(produtoRepository.findAll())
                .thenReturn(List.of(produto(1L, "Motor")));

        List<ProdutoResponse> resultado = produtoService.listar("   ");

        assertEquals(1, resultado.size());

        verify(produtoRepository).findAll();
        verify(produtoRepository, never())
                .findByNomeContainingIgnoreCase(anyString());
    }

    @Test
    void givenIdExistenteQuandoBuscaPorIdDtoEntaoRetornaDto() {
        when(produtoRepository.findById(5L))
                .thenReturn(Optional.of(produto(5L, "Portao")));

        ProdutoDTO resultado = produtoService.buscarPorIdDTO(5L);

        assertEquals("Portao", resultado.getNome());
        assertEquals(new BigDecimal("25.90"), resultado.getPreco());
    }

    @Test
    void givenIdExistenteQuandoBuscaPorIdEntaoRetornaResponse() {
        when(produtoRepository.findById(5L))
                .thenReturn(Optional.of(produto(5L, "Portao")));

        ProdutoResponse resultado = produtoService.buscarPorId(5L);

        assertEquals(5L, resultado.id());
        assertEquals("Portao", resultado.nome());
    }

    @Test
    void givenIdSemProdutoQuandoBuscaPorIdEntaoLancaRecursoNaoEncontrado() {
        when(produtoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.buscarPorId(99L)
        );
    }

    @Test
    void givenIdExistenteQuandoAtualizaDtoEntaoSalvaAlteracoes() {
        Produto existente = produto(5L, "Portao antigo");

        ProdutoDTO entrada = new ProdutoDTO(
                "Portao novo", "Descricao nova",
                new BigDecimal("300.00"), 8, 2
        );

        when(produtoRepository.findById(5L))
                .thenReturn(Optional.of(existente));

        when(produtoRepository.save(any(Produto.class)))
                .thenAnswer(call -> call.getArgument(0));

        ProdutoDTO resultado = produtoService.atualizar(5L, entrada);

        assertEquals("Portao novo", resultado.getNome());
        assertEquals("Descricao nova", resultado.getDescricao());
        assertEquals(new BigDecimal("300.00"), resultado.getPreco());
        assertEquals(8, resultado.getQuantidadeEstoque());
        assertEquals(2, resultado.getEstoqueMinimo());

        verify(produtoRepository).save(existente);
    }

    @Test
    void givenIdInexistenteQuandoAtualizaDtoEntaoLancaExcecao() {
        when(produtoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ProdutoDTO entrada = new ProdutoDTO(
                "Motor", "Teste", BigDecimal.TEN, 5, 1
        );

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.atualizar(99L, entrada)
        );

        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void givenIdExistenteQuandoAtualizaProdutoEntaoSalvaNovosDados() {
        Produto existente = produto(5L, "Portao antigo");

        ProdutoRequest request = new ProdutoRequest(
                "Portao novo", 8L, "Atualizado",
                new BigDecimal("250.00"), 9, 2, null
        );

        when(produtoRepository.findById(5L))
                .thenReturn(Optional.of(existente));

        when(produtoRepository.save(any(Produto.class)))
                .thenAnswer(call -> call.getArgument(0));

        ProdutoResponse resultado = produtoService.atualizar(5L, request);

        assertEquals("Portao novo", resultado.nome());
        assertEquals(8L, resultado.codigo());
        assertEquals(new BigDecimal("250.00"), resultado.preco());
        assertEquals(9, resultado.quantidadeEstoque());
        assertEquals(2, resultado.estoqueMinimo());

        verify(produtoRepository).save(existente);
    }

    @Test
    void givenIdInexistenteQuandoAtualizaRequestEntaoLancaExcecao() {
        when(produtoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ProdutoRequest request = new ProdutoRequest(
                "Motor", 10L, "Teste", BigDecimal.TEN, 5, 1, null
        );

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.atualizar(99L, request)
        );

        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void givenIdInexistenteQuandoExcluiProdutoEntaoLancaExcecaoESemApagar() {
        when(produtoRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.excluir(99L)
        );

        verify(produtoRepository, never()).deleteById(anyLong());
    }

    @Test
    void givenProdutoExistenteQuandoExcluiEntaoRemoveDoRepositorio() {
        when(produtoRepository.existsById(5L)).thenReturn(true);

        produtoService.excluir(5L);

        verify(produtoRepository).existsById(5L);
        verify(produtoRepository).deleteById(5L);
    }

    @Test
    void givenIdExistenteQuandoDeletaEntaoExcluiProduto() {
        when(produtoRepository.existsById(5L)).thenReturn(true);

        produtoService.deletar(5L);

        verify(produtoRepository).existsById(5L);
        verify(produtoRepository).deleteById(5L);
    }

    @Test
    void givenProdutosComBaixoEstoqueQuandoConsultaEntaoRetornaResponsesCorrespondentes() {
        when(produtoRepository.findProdutosComBaixoEstoque())
                .thenReturn(List.of(produto(2L, "Sensor")));

        List<ProdutoResponse> resultado =
                produtoService.listarProdutosComBaixoEstoqueResponse();

        assertEquals(1, resultado.size());
        assertEquals("Sensor", resultado.get(0).nome());

        verify(produtoRepository).findProdutosComBaixoEstoque();
    }

    @Test
    void givenProdutosComBaixoEstoqueQuandoListaDtoEntaoRetornaListaConvertida() {
        when(produtoRepository.findProdutosComBaixoEstoque())
                .thenReturn(List.of(produto(2L, "Sensor")));

        List<ProdutoDTO> resultado =
                produtoService.listarProdutosComBaixoEstoque();

        assertEquals(1, resultado.size());
        assertEquals("Sensor", resultado.get(0).getNome());

        verify(produtoRepository).findProdutosComBaixoEstoque();
    }

    @Test
    void givenNenhumProdutoComBaixoEstoqueQuandoConsultaEntaoRetornaListaVazia() {
        when(produtoRepository.findProdutosComBaixoEstoque())
                .thenReturn(List.of());

        assertTrue(produtoService.listarProdutosComBaixoEstoqueResponse().isEmpty());
        assertTrue(produtoService.listarProdutosComBaixoEstoque().isEmpty());
    }

    private Produto produto(Long id, String nome) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome(nome);
        produto.setCodigo(10L);
        produto.setDescricao("Teste");
        produto.setPreco(new BigDecimal("25.90"));
        produto.setQuantidadeEstoque(2);
        produto.setEstoqueMinimo(3);
        return produto;
    }
}