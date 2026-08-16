package com.example.amportoes.service;

import com.example.amportoes.controller.request.OrcamentoRequest;
import com.example.amportoes.controller.response.OrcamentoResponse;
import com.example.amportoes.entity.Orcamento;
import com.example.amportoes.entity.StatusTarefa;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.repository.OrcamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;

    public OrcamentoService(OrcamentoRepository orcamentoRepository) {
        this.orcamentoRepository = orcamentoRepository;
    }

    public OrcamentoResponse criar(OrcamentoRequest request) {
        Orcamento orcamento = request.toEntity();
        orcamento.setStatus(StatusTarefa.PENDENTE);

        Orcamento salvo = orcamentoRepository.save(orcamento);
        return OrcamentoResponse.fromEntity(salvo);
    }

    public List<OrcamentoResponse> listar() {
        return orcamentoRepository.findAll()
                .stream()
                .map(OrcamentoResponse::fromEntity)
                .toList();
    }

    public OrcamentoResponse buscarPorId(Long id) {
        Orcamento orcamento = buscarEntidadePorId(id);
        return OrcamentoResponse.fromEntity(orcamento);
    }

    public OrcamentoResponse atualizar(Long id, OrcamentoRequest request) {
        Orcamento orcamento = buscarEntidadePorId(id);

        orcamento.setTitulo(request.titulo());
        orcamento.setDescricao(request.descricao());
        orcamento.setDataVencimento(request.dataVencimento());

        Orcamento atualizado = orcamentoRepository.save(orcamento);
        return OrcamentoResponse.fromEntity(atualizado);
    }

    public void deletar(Long id) {
        if (!orcamentoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Orçamento não encontrado");
        }
        orcamentoRepository.deleteById(id);
    }

    private Orcamento buscarEntidadePorId(Long id) {
        return orcamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento não encontrado"));
    }
}
