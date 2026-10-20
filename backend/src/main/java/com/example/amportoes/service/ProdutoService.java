
package com.example.amportoes.service;

import com.example.amportoes.controller.request.ProdutoRequest;
import com.example.amportoes.controller.response.ProdutoResponse;
import com.example.amportoes.dto.ProdutoDTO;
import com.example.amportoes.entity.Produto;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    // CRUD legado utilizando ProdutoDTO

    public ProdutoDTO criar(ProdutoDTO dto) {
        return toDTO(produtoRepository.save(toEntity(dto)));
    }

    public List<ProdutoDTO> listar() {
        return produtoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public ProdutoDTO buscarPorIdDTO(Long id) {
        return toDTO(buscarEntidadePorId(id));
    }

    @Transactional
    public ProdutoDTO atualizar(Long id, ProdutoDTO dto) {
        Produto produto = buscarEntidadePorId(id);
        atualizarEntidade(produto, dto);

        return toDTO(produtoRepository.save(produto));
    }

    public void excluir(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException(
                    "Produto não foi encontrado"
            );
        }

        produtoRepository.deleteById(id);
    }

    public List<ProdutoDTO> listarProdutosComBaixoEstoque() {
        return produtoRepository.findProdutosComBaixoEstoque()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // CRUD atual utilizando ProdutoRequest e ProdutoResponse

    public ProdutoResponse criar(ProdutoRequest request) {
        Produto produto = request.toEntity();
        Produto salvo = produtoRepository.save(produto);

        return ProdutoResponse.fromEntity(salvo);
    }

    public List<ProdutoResponse> listar(String nome) {
        List<Produto> produtos =
                (nome == null || nome.isBlank())
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

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarEntidadePorId(id);

        produto.setNome(request.nome());
        produto.setCodigo(request.codigo());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setQuantidadeEstoque(request.quantidadeEstoque());
        produto.setEstoqueMinimo(request.estoqueMinimo());
        produto.setDataValidade(request.dataValidade());

        Produto atualizado = produtoRepository.save(produto);

        return ProdutoResponse.fromEntity(atualizado);
    }

    public void deletar(Long id) {
        excluir(id);
    }

    // Consulta de baixo estoque utilizando o PostgreSQL via JPQL

    public List<ProdutoResponse> listarProdutosComBaixoEstoqueResponse() {
        return produtoRepository.findProdutosComBaixoEstoque()
                .stream()
                .map(ProdutoResponse::fromEntity)
                .toList();
    }

    // Métodos auxiliares

    private Produto buscarEntidadePorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Produto não foi encontrado"
                        )
                );
    }

    private Produto toEntity(ProdutoDTO dto) {
        Produto produto = new Produto();
        atualizarEntidade(produto, dto);

        return produto;
    }

    private void atualizarEntidade(Produto produto, ProdutoDTO dto) {
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setQuantidadeEstoque(dto.getQuantidadeEstoque());
        produto.setEstoqueMinimo(dto.getEstoqueMinimo());
    }

    private ProdutoDTO toDTO(Produto produto) {
        return new ProdutoDTO(
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getQuantidadeEstoque(),
                produto.getEstoqueMinimo()
        );
    }
}
