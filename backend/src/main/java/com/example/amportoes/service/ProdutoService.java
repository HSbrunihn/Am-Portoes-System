package com.example.amportoes.service;

import com.example.amportoes.controller.request.ProdutoRequest;
import com.example.amportoes.controller.response.ProdutoResponse;
import com.example.amportoes.entity.Produto;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public ProdutoResponse criar(ProdutoRequest request) {
        Produto produto = request.toEntity();
        Produto salvo = produtoRepository.save(produto);
        return ProdutoResponse.fromEntity(salvo);
    }

    public List<ProdutoResponse> listar(String nome) {
        List<Produto> produtos = (nome == null || nome.isBlank())
                ? produtoRepository.findAll()
                : produtoRepository.findByNomeContainingIgnoreCase(nome);

        return produtos.stream()
                .map(ProdutoResponse::fromEntity)
                .toList();
    }

    public ProdutoResponse buscarPorId(Long id) {
        Produto produto = buscarEntidadePorId(id);
        return ProdutoResponse.fromEntity(produto);
    }

    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarEntidadePorId(id);

        produto.setNome(request.nome());
        produto.setCodigo(request.codigo());
        produto.setPreco(request.preco());
        produto.setDataValidade(request.dataValidade());

        Produto atualizado = produtoRepository.save(produto);
        return ProdutoResponse.fromEntity(atualizado);
    }

    public void deletar(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Produto não foi encontrado");
        }
        produtoRepository.deleteById(id);
    }

    private Produto buscarEntidadePorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não foi encontrado"));
    }
}
