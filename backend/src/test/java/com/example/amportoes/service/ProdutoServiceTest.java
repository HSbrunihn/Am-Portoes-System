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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {
    @Mock ProdutoRepository produtoRepository;
    @InjectMocks ProdutoService produtoService;

    @Test
    void givenDadosDoDtoQuandoCriaProdutoEntaoPersisteEDevolveDadosConvertidos() {
        // Given
        ProdutoDTO entrada = new ProdutoDTO("Sensor", "Teste", new BigDecimal("25.90"), 10, 2);
        when(produtoRepository.save(any(Produto.class))).thenAnswer(call -> call.getArgument(0));

        // When
        ProdutoDTO resultado = produtoService.criar(entrada);

        // Then
        assertEquals("Sensor", resultado.getNome());
        assertEquals(new BigDecimal("25.90"), resultado.getPreco());
        verify(produtoRepository).save(argThat(p -> p.getQuantidadeEstoque() == 10));
    }

    @Test
    void givenRequisicaoValidaQuandoCriaProdutoEntaoRetornaResponseDoProdutoSalvo() {
        // Given
        ProdutoRequest request = new ProdutoRequest("Portao", 7L, "Basculante", new BigDecimal("100.00"), 4, 1, null);
        when(produtoRepository.save(any(Produto.class))).thenAnswer(call -> {
            Produto salvo = call.getArgument(0);
            salvo.setId(3L);
            return salvo;
        });

        // When
        ProdutoResponse resultado = produtoService.criar(request);

        // Then
        assertEquals(3L, resultado.id());
        assertEquals("Portao", resultado.nome());
    }

    @Test
    void givenNomeDeBuscaQuandoListaProdutosEntaoUsaBuscaParcialIgnorandoCaixa() {
        // Given
        Produto produto = produto(1L, "Motor");
        when(produtoRepository.findByNomeContainingIgnoreCase("mot")).thenReturn(List.of(produto));

        // When
        List<ProdutoResponse> resultado = produtoService.listar("mot");

        // Then
        assertEquals(1, resultado.size());
        assertEquals("Motor", resultado.get(0).nome());
        verify(produtoRepository).findByNomeContainingIgnoreCase("mot");
        verify(produtoRepository, never()).findAll();
    }

    @Test
    void givenIdSemProdutoQuandoBuscaPorIdEntaoLancaRecursoNaoEncontrado() {
        // Given
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class, () -> produtoService.buscarPorId(99L));

        // Then
        assertNotNull(excecao);
    }

    @Test
    void givenIdExistenteQuandoAtualizaProdutoEntaoSalvaNovosDados() {
        // Given
        Produto existente = produto(5L, "Portao antigo");
        ProdutoRequest request = new ProdutoRequest("Portao novo", 8L, "Atualizado", new BigDecimal("250.00"), 9, 2, null);
        when(produtoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(call -> call.getArgument(0));

        // When
        ProdutoResponse resultado = produtoService.atualizar(5L, request);

        // Then
        assertEquals("Portao novo", resultado.nome());
        assertEquals(8L, resultado.codigo());
        verify(produtoRepository).save(existente);
    }

    @Test
    void givenIdInexistenteQuandoExcluiProdutoEntaoLancaExcecaoESemApagar() {
        // Given
        when(produtoRepository.existsById(99L)).thenReturn(false);

        // When
        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class, () -> produtoService.excluir(99L));

        // Then
        assertNotNull(excecao);
        verify(produtoRepository, never()).deleteById(anyLong());
    }

    @Test
    void givenProdutosComBaixoEstoqueQuandoConsultaEntaoRetornaResponsesCorrespondentes() {
        // Given
        when(produtoRepository.findProdutosComBaixoEstoque()).thenReturn(List.of(produto(2L, "Sensor")));

        // When
        List<ProdutoResponse> resultado = produtoService.listarProdutosComBaixoEstoqueResponse();

        // Then
        assertEquals(1, resultado.size());
        assertEquals("Sensor", resultado.get(0).nome());
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