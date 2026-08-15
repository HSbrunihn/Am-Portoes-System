import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Task } from '../../models/task.model';
import { TaskService } from '../../services/task';

@Component({
  selector: 'app-task-form',
  imports: [FormsModule],
  templateUrl: './task-form.html',
  styleUrl: './task-form.scss',
})
export class TaskForm {
  private taskService = inject(TaskService);
  novaTask: Partial<Task> = {};

  onSalvar(): void {
    const task: Task = {
      id: Date.now(),
      nome: this.novaTask.nome ?? '',
      status: this.novaTask.status ?? 'Aguardando',
      prioridade: this.novaTask.prioridade ?? 'Média',
      valor: this.novaTask.valor ?? 0,
      descricao: this.novaTask.descricao ?? '',
      produtos: []
    };

    this.taskService.addTask(task);
    this.novaTask = {};
  }
}