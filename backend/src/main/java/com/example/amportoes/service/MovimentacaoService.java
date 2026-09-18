package com.example.amportoes.service;

import com.example.amportoes.dto.MovimentacaoDTO;
import com.example.amportoes.entity.MovimentacaoEstoque;
import com.example.amportoes.entity.Produto;
import com.example.amportoes.entity.TipoMovimentacao;
import com.example.amportoes.repository.MovimentacaoRepository;
import com.example.amportoes.repository.ProdutoRepository;
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
        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (dto.getTipo() == TipoMovimentacao.SAIDA) {
            if (produto.getQuantidadeEstoque() == null || produto.getQuantidadeEstoque() < dto.getQuantidade()) {
                throw new IllegalArgumentException("Estoque insuficiente para saída");
            }
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - dto.getQuantidade());
        } else if (dto.getTipo() == TipoMovimentacao.ENTRADA) {
            produto.setQuantidadeEstoque((produto.getQuantidadeEstoque() == null ? 0 : produto.getQuantidadeEstoque()) + dto.getQuantidade());
        } else {
            throw new RuntimeException("Tipo de movimentação inválido");
        }

        produtoRepository.save(produto);

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setProduto(produto);
        movimentacao.setQuantidade(dto.getQuantidade());
        movimentacao.setTipo(dto.getTipo());
        movimentacao.setDataMovimentacao(dto.getDataMovimentacao() != null ? dto.getDataMovimentacao() : LocalDateTime.now());
        movimentacao.setObservacao(dto.getObservacao());

        return movimentacaoRepository.save(movimentacao);
    }
}