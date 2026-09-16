import { Injectable, signal } from '@angular/core';
import { Observable, of } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { StatusTarefa, Tarefa, TarefaRequest } from '../models/api.model';
import { Task } from '../models/task.model';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private readonly tasks = signal<Task[]>([]);

  constructor(private readonly api: ApiService) {
    this.api.list<Tarefa>('tarefas').subscribe({
      next: tarefas => this.tasks.set(tarefas.map(tarefa => this.toTask(tarefa))),
    });
  }

  getTasks(): Task[] {
    return this.tasks();
  }

  addTask(task: Task): Observable<Task> {
    return this.api.create<Tarefa>('tarefas', this.toRequest(task)).pipe(
      switchMap(created => this.updateStatusIfNeeded(created, task.status)),
      tap(created => this.tasks.update(tasks => [...tasks, this.toTask(created)])),
      switchMap(created => of(this.toTask(created))),
    );
  }

  deleteTask(id: number): Observable<void> {
    return this.api.delete('tarefas', id).pipe(
      tap(() => this.tasks.update(tasks => tasks.filter(task => task.id !== id))),
    );
  }

  updateTask(taskAtualizada: Task): Observable<Task> {
    return this.api.update<Tarefa>('tarefas', taskAtualizada.id, this.toRequest(taskAtualizada)).pipe(
      switchMap(updated => this.updateStatusIfNeeded(updated, taskAtualizada.status)),
      tap(updated => this.tasks.update(tasks => tasks.map(task =>
        task.id === taskAtualizada.id ? this.toTask(updated) : task
      ))),
      switchMap(updated => of(this.toTask(updated))),
    );
  }

  private toTask(tarefa: Tarefa): Task {
    return {
      id: tarefa.id,
      nome: tarefa.titulo,
      status: {
        PENDENTE: 'Aguardando',
        EM_ANDAMENTO: 'Em andamento',
        CONCLUIDA: 'Concluída',
      }[tarefa.status] as Task['status'],
      prioridade: 'Média',
      valor: tarefa.valor ?? 0,
      descricao: tarefa.descricao ?? '',
      produtos: [],
      dataVencimento: tarefa.dataVencimento,
    };
  }

  private toRequest(task: Task): TarefaRequest {
    return {
      titulo: task.nome,
      descricao: task.descricao,
      valor: task.valor,
      dataVencimento: task.dataVencimento ?? new Date().toISOString().slice(0, 10),
    };
  }

  private updateStatusIfNeeded(tarefa: Tarefa, status: Task['status']): Observable<Tarefa> {
    const backendStatus = this.toBackendStatus(status);
    return backendStatus === tarefa.status
      ? of(tarefa)
      : this.api.updateTaskStatus(tarefa.id, backendStatus);
  }

  private toBackendStatus(status: Task['status']): StatusTarefa {
    return {
      'Aguardando': 'PENDENTE',
      'Em andamento': 'EM_ANDAMENTO',
      'Concluída': 'CONCLUIDA',
    }[status] as StatusTarefa;
  }
}