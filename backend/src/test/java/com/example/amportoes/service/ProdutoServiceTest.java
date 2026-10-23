
package com.example.amportoes.service;

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

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private ProdutoDTO dto() {
        return new ProdutoDTO(
                "Sensor",
                "Sensor de teste",
                new BigDecimal("25.90"),
                10,
                2
        );
    }

    private Produto produto() {
        Produto produto = new Produto();
        produto.setNome("Sensor");
        produto.setDescricao("Sensor de teste");
        produto.setPreco(new BigDecimal("25.90"));
        produto.setQuantidadeEstoque(10);
        produto.setEstoqueMinimo(2);
        return produto;
    }

    @Test
    void deveCriarProdutoComDto() {
        when(produtoRepository.save(any(Produto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProdutoDTO resultado = produtoService.criar(dto());

        assertEquals("Sensor", resultado.getNome());
        assertEquals(new BigDecimal("25.90"), resultado.getPreco());
        assertEquals(10, resultado.getQuantidadeEstoque());
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    void deveListarProdutosComDto() {
        when(produtoRepository.findAll()).thenReturn(List.of(produto()));

        List<ProdutoDTO> resultado = produtoService.listar();

        assertEquals(1, resultado.size());
        assertEquals("Sensor", resultado.get(0).getNome());
    }

    @Test
    void deveRetornarListaVaziaAoListarComDto() {
        when(produtoRepository.findAll()).thenReturn(List.of());

        assertTrue(produtoService.listar().isEmpty());
    }

    @Test
    void deveBuscarProdutoPorIdComDto() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto()));

        ProdutoDTO resultado = produtoService.buscarPorIdDTO(1L);

        assertEquals("Sensor", resultado.getNome());
        assertEquals(10, resultado.getQuantidadeEstoque());
    }

    @Test
    void deveLancarExcecaoAoBuscarProdutoInexistenteComDto() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.buscarPorIdDTO(99L)
        );
    }

    @Test
    void deveAtualizarProdutoComDto() {
        Produto existente = produto();
        ProdutoDTO atualizacao = new ProdutoDTO(
                "Sensor atualizado",
                "Descrição atualizada",
                new BigDecimal("39.90"),
                15,
                3
        );

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(produtoRepository.save(any(Produto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProdutoDTO resultado = produtoService.atualizar(1L, atualizacao);

        assertEquals("Sensor atualizado", resultado.getNome());
        assertEquals(new BigDecimal("39.90"), resultado.getPreco());
        assertEquals(15, resultado.getQuantidadeEstoque());
        verify(produtoRepository).save(existente);
    }

    @Test
    void deveLancarExcecaoAoAtualizarProdutoInexistenteComDto() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.atualizar(99L, dto())
        );

        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void deveExcluirProdutoExistenteComDto() {
        when(produtoRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> produtoService.excluir(1L));

        verify(produtoRepository).deleteById(1L);
    }

    @Test
    void deveLancarExcecaoAoExcluirProdutoInexistenteComDto() {
        when(produtoRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.excluir(99L)
        );

        verify(produtoRepository, never()).deleteById(anyLong());
    }

    @Test
    void deveListarProdutosComBaixoEstoqueEmDto() {
        when(produtoRepository.findProdutosComBaixoEstoque())
                .thenReturn(List.of(produto()));

        List<ProdutoDTO> resultado =
                produtoService.listarProdutosComBaixoEstoque();

        assertEquals(1, resultado.size());
        assertEquals("Sensor", resultado.get(0).getNome());
    }

    @Test
    void deveRetornarListaVaziaDeProdutosComBaixoEstoqueEmDto() {
        when(produtoRepository.findProdutosComBaixoEstoque())
                .thenReturn(List.of());

        assertTrue(produtoService.listarProdutosComBaixoEstoque().isEmpty());
    }
}
