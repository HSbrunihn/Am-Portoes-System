import { Injectable, signal } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { StatusTarefa, Tarefa, TarefaRequest } from '../models/api.model';
import { Task } from '../models/task.model';

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  private readonly tasks = signal<Task[]>([]);

  constructor(private readonly api: ApiService) {
    this.reload();
  }

  reload(): void {
    this.api.list<Tarefa>('tarefas').subscribe({
      next: (tarefas) => this.tasks.set(tarefas.map((t) => this.toTask(t))),
      error: () => this.tasks.set([]),
    });
  }

  getTasks(): Task[] {
    return this.tasks();
  }

  addTask(task: Task): Observable<Task> {
    return this.api.create<Tarefa>('tarefas', this.toRequest(task)).pipe(
      switchMap((created) => this.updateStatusIfNeeded(created, task.status)),
      tap(() => this.reload()),
      switchMap((created) => of(this.toTask(created))),
    );
  }

  createTask(dados: Partial<Task>): Observable<Task> {
    const novaTask: Task = {
      id: 0,
      nome: dados.nome || dados.titulo || '',
      status: dados.status || 'Pendente',
      prioridade: dados.prioridade || 'Média',
      valor: dados.valor || 0,
      descricao: dados.descricao || '',
      produtos: dados.produtos || [],
      dataVencimento: dados.dataVencimento,
    } as Task;

    return this.addTask(novaTask);
  }

  deleteTask(id: number): Observable<void> {
    return this.api.delete('tarefas', id).pipe(
      tap(() => this.reload()),
    );
  }

  updateTask(taskAtualizada: Task): Observable<Task>;
  updateTask(id: number, dados: Partial<Task>): Observable<Task>;
  updateTask(param1: Task | number, param2?: Partial<Task>): Observable<Task> {
    let task: Task;

    if (typeof param1 === 'number') {
      const existente = this.tasks().find((t) => t.id === param1);
      task = {
        ...existente,
        ...param2,
        id: param1,
      } as Task;
    } else {
      task = param1;
    }

    return this.api
      .update<Tarefa>('tarefas', task.id, this.toRequest(task))
      .pipe(
        switchMap((updated) => this.updateStatusIfNeeded(updated, task.status)),
        tap(() => this.reload()),
        switchMap((updated) => of(this.toTask(updated))),
      );
  }

  private toTask(tarefa: Tarefa): Task {
    return {
      id: tarefa.id,
      nome: tarefa.titulo,
      status: {
        PENDENTE: 'Pendente',
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

  private updateStatusIfNeeded(
    tarefa: Tarefa,
    status: Task['status'],
  ): Observable<Tarefa> {
    const backendStatus = this.toBackendStatus(status);
    if (backendStatus === tarefa.status) {
      return of(tarefa);
    }
    return this.api.updateTaskStatus(tarefa.id, backendStatus).pipe(
      catchError(() => of(tarefa)),
    );
  }

  private toBackendStatus(status: Task['status']): StatusTarefa {
    const map: Record<string, StatusTarefa> = {
      Pendente: 'PENDENTE',
      Aguardando: 'PENDENTE',
      'Em andamento': 'EM_ANDAMENTO',
      Concluída: 'CONCLUIDA',
    };
    return map[status] ?? 'PENDENTE';
  }
}