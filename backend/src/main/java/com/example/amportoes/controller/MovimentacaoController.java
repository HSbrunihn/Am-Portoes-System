package com.seuprojeto.service;

import com.seuprojeto.dto.MovimentacaoDTO;
import com.seuprojeto.entity.MovimentacaoEstoque;
import com.seuprojeto.entity.Produto;
import com.seuprojeto.entity.TipoMovimentacao;
import com.seuprojeto.repository.MovimentacaoRepository;
import com.seuprojeto.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;

    public MovimentacaoService(MovimentacaoRepository movimentacaoRepository,
                               ProdutoRepository produtoRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public MovimentacaoEstoque registrarMovimentacao(MovimentacaoDTO dto) {
        // Buscar produto
        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Regras de negócio
        if (dto.getTipo() == TipoMovimentacao.SAIDA) {
            if (produto.getQuantidade() < dto.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para saída");
            }
            produto.setQuantidade(produto.getQuantidade() - dto.getQuantidade());
        } else if (dto.getTipo() == TipoMovimentacao.ENTRADA) {
            produto.setQuantidade(produto.getQuantidade() + dto.getQuantidade());
        } else {
            throw new RuntimeException("Tipo de movimentação inválido");
        }

        // Atualizar produto
        produtoRepository.save(produto);

        // Registrar movimentação
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setProduto(produto);
        movimentacao.setQuantidade(dto.getQuantidade());
        movimentacao.setTipo(dto.getTipo());
        movimentacao.setDataMovimentacao(dto.getDataMovimentacao() != null ? dto.getDataMovimentacao() : LocalDateTime.now());
        movimentacao.setObservacao(dto.getObservacao());

        return movimentacaoRepository.save(movimentacao);
    }
}