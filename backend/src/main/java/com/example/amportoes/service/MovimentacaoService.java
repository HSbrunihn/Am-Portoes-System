
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;

    public MovimentacaoService(
            MovimentacaoRepository movimentacaoRepository,
            ProdutoRepository produtoRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.produtoRepository = produtoRepository;
    }

    private MovimentacaoResponseDTO converterParaResponse(
            MovimentacaoEstoque movimentacao) {

        return new MovimentacaoResponseDTO(
                movimentacao.getId(),
                movimentacao.getProduto().getId(),
                movimentacao.getProduto().getNome(),
                movimentacao.getQuantidade(),
                movimentacao.getTipo(),
                movimentacao.getObservacao(),
                movimentacao.getDataMovimentacao()
        );
    }

    @Transactional
    public MovimentacaoResponseDTO registrarMovimentacao(
            MovimentacaoDTO dto) {

        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Produto não encontrado"));

        if (dto.getTipo() == TipoMovimentacao.SAIDA) {
            if (produto.getQuantidadeEstoque() == null
                    || produto.getQuantidadeEstoque() < dto.getQuantidade()) {
                throw new RegraNegocioException(
                        "Estoque insuficiente para saída");
            }

            produto.setQuantidadeEstoque(
                    produto.getQuantidadeEstoque() - dto.getQuantidade());

        } else if (dto.getTipo() == TipoMovimentacao.ENTRADA) {
            int estoqueAtual = produto.getQuantidadeEstoque() == null
                    ? 0
                    : produto.getQuantidadeEstoque();

            produto.setQuantidadeEstoque(
                    estoqueAtual + dto.getQuantidade());

        } else {
            throw new RegraNegocioException(
                    "Tipo de movimentação inválido");
        }

        produtoRepository.save(produto);

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setProduto(produto);
        movimentacao.setQuantidade(dto.getQuantidade());
        movimentacao.setTipo(dto.getTipo());
        movimentacao.setDataMovimentacao(
                dto.getDataMovimentacao() != null
                        ? dto.getDataMovimentacao()
                        : LocalDateTime.now());
        movimentacao.setObservacao(dto.getObservacao());

        MovimentacaoEstoque salva =
                movimentacaoRepository.save(movimentacao);

        return converterParaResponse(salva);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponseDTO> listarMovimentacoes() {
        return movimentacaoRepository.findAll()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponseDTO> listarPorProduto(
            Long produtoId) {

        if (!produtoRepository.existsById(produtoId)) {
            throw new RecursoNaoEncontradoException(
                    "Produto não encontrado");
        }

        return movimentacaoRepository
                .findByProdutoIdOrderByDataMovimentacaoDesc(produtoId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }
}
