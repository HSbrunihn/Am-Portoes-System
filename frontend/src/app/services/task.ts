import { Injectable, signal } from '@angular/core';
import { ApiService } from './api.service';
import { Tarefa } from '../models/api.model';
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

  addTask(task: Task): void {
    this.tasks.update(tasks => [...tasks, task]);
  }

  deleteTask(id: number): void {
    this.tasks.update(tasks => tasks.filter(task => task.id !== id));
  }

  updateTask(taskAtualizada: Task): void {
    this.tasks.update(tasks => tasks.map(task =>
      task.id === taskAtualizada.id ? taskAtualizada : task
    ));
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
      valor: 0,
      descricao: tarefa.descricao ?? '',
      produtos: [],
    };
  }
}