package com.example.amportoes.dto;

import com.example.amportoes.entity.TipoMovimentacao;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MovimentacaoDTOTest {

    @Test
    void deveCriarDtoComConstrutorPadrao() {
        MovimentacaoDTO dto = new MovimentacaoDTO();

        assertNotNull(dto);
        assertNull(dto.getProdutoId());
        assertNull(dto.getQuantidade());
        assertNull(dto.getTipo());
        assertNull(dto.getObservacao());
        assertNull(dto.getDataMovimentacao());
    }

    @Test
    void deveDefinirERecuperarProdutoId() {
        MovimentacaoDTO dto = new MovimentacaoDTO();

        dto.setProdutoId(10L);

        assertEquals(10L, dto.getProdutoId());
    }

    @Test
    void deveDefinirERecuperarQuantidade() {
        MovimentacaoDTO dto = new MovimentacaoDTO();

        dto.setQuantidade(5);

        assertEquals(5, dto.getQuantidade());
    }

    @Test
    void deveDefinirERecuperarTipo() {
        MovimentacaoDTO dto = new MovimentacaoDTO();

        dto.setTipo(TipoMovimentacao.ENTRADA);

        assertEquals(TipoMovimentacao.ENTRADA, dto.getTipo());
    }

    @Test
    void deveDefinirERecuperarObservacao() {
        MovimentacaoDTO dto = new MovimentacaoDTO();

        dto.setObservacao("Entrada de material");

        assertEquals("Entrada de material", dto.getObservacao());
    }

    @Test
    void deveDefinirERecuperarDataMovimentacao() {
        MovimentacaoDTO dto = new MovimentacaoDTO();
        LocalDateTime data = LocalDateTime.of(2026, 10, 23, 14, 30);

        dto.setDataMovimentacao(data);

        assertEquals(data, dto.getDataMovimentacao());
    }
}