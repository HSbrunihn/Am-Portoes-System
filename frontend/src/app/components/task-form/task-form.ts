import { Component, inject, Input, OnChanges, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Task } from '../../models/task.model';
import { TaskService } from '../../services/task';

@Component({
  selector: 'app-task-form',
  imports: [FormsModule],
  templateUrl: './task-form.html',
  styleUrl: './task-form.scss',
})
export class TaskForm implements OnChanges {
  private taskService = inject(TaskService);

  @Input() taskParaEditar: Task | null = null;
  @Output() concluido = new EventEmitter<void>();

  novaTask: Partial<Task> = {};
  saving = false;

  ngOnChanges(): void {
    if (this.taskParaEditar) {
      this.novaTask = { ...this.taskParaEditar };
    }
  }

  onSalvar(): void {
    if (this.saving) return;

    const task: Task = {
      id: this.taskParaEditar?.id ?? Date.now(),
      nome: this.novaTask.nome ?? '',
      status: this.novaTask.status ?? 'Aguardando',
      prioridade: this.novaTask.prioridade ?? 'Média',
      valor: this.novaTask.valor ?? 0,
      descricao: this.novaTask.descricao ?? '',
      produtos: this.taskParaEditar?.produtos ?? [],
      dataVencimento: this.novaTask.dataVencimento ?? new Date().toISOString().slice(0, 10),
    };

    this.saving = true;
    if (this.taskParaEditar) {
      this.taskService.updateTask(task).subscribe({
        next: () => this.finalizar(),
        error: () => this.saving = false,
      });
    } else {
      this.taskService.addTask(task).subscribe({
        next: () => this.finalizar(),
        error: () => this.saving = false,
      });
    }
  }

  private finalizar(): void {
    this.saving = false;
    this.novaTask = {};
    this.concluido.emit();
  }
}