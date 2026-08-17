package com.example.amportoes.service;

import com.example.amportoes.controller.request.TarefaRequest;
import com.example.amportoes.controller.response.TarefaResponse;
import com.example.amportoes.entity.StatusTarefa;
import com.example.amportoes.entity.Tarefa;
import com.example.amportoes.exception.RecursoNaoEncontradoException;
import com.example.amportoes.repository.TarefaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;

    public TarefaService(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public TarefaResponse criar(TarefaRequest request) {
        Tarefa tarefa = request.toEntity();
        tarefa.setStatus(StatusTarefa.PENDENTE);

        Tarefa salva = tarefaRepository.save(tarefa);
        return toResponse(salva);
    }

    public List<TarefaResponse> listar(StatusTarefa status) {
        List<Tarefa> tarefas = (status == null)
                ? tarefaRepository.findAll()
                : tarefaRepository.findByStatus(status);

        return tarefas.stream()
                .map(this::toResponse)
                .toList();
    }

    public TarefaResponse buscarPorId(Long id) {
        Tarefa tarefa = buscarEntidadePorId(id);
        return toResponse(tarefa);
    }

    public TarefaResponse atualizar(Long id, TarefaRequest request) {
        Tarefa tarefa = buscarEntidadePorId(id);

        tarefa.setTitulo(request.titulo());
        tarefa.setDescricao(request.descricao());
        tarefa.setDataVencimento(request.dataVencimento());

        Tarefa atualizada = tarefaRepository.save(tarefa);
        return toResponse(atualizada);
    }

    public TarefaResponse atualizarStatus(Long id, StatusTarefa status) {
        Tarefa tarefa = buscarEntidadePorId(id);
        tarefa.setStatus(status);

        Tarefa atualizada = tarefaRepository.save(tarefa);
        return toResponse(atualizada);
    }

    public void deletar(Long id) {
        if (!tarefaRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Tarefa não foi encontrada");
        }
        tarefaRepository.deleteById(id);
    }

    private Tarefa buscarEntidadePorId(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tarefa não foi encontrada"));
    }

    private TarefaResponse toResponse(Tarefa tarefa) {
        return TarefaResponse.fromEntity(tarefa);
    }
}
