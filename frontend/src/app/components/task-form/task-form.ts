import { Component, Input, Output, EventEmitter, inject, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaskService } from '../../services/task';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './task-form.html',
  styleUrl: './task-form.scss',
})
export class TaskForm implements OnChanges {
  protected readonly taskService = inject(TaskService);

  @Input() taskParaEditar: Task | null = null;
  @Output() concluido = new EventEmitter<void>();

  protected saving = false;

  protected novaTask: Partial<Task> = {
    nome: '',
    valor: 0,
    descricao: '',
    dataVencimento: new Date().toISOString().slice(0, 10),
    status: 'Pendente',
    prioridade: 'Média',
  };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['taskParaEditar'] && this.taskParaEditar) {
      this.novaTask = { ...this.taskParaEditar };
    }
  }

  protected onSalvar(): void {
    if (this.saving) return;

    this.saving = true;

    if (this.taskParaEditar && this.taskParaEditar.id) {
      this.taskService.updateTask(this.taskParaEditar.id, this.novaTask).subscribe({
        next: () => {
          this.saving = false;
          this.concluido.emit();
        },
        error: () => {
          this.saving = false;
        },
      });
    } else {
      this.taskService.createTask(this.novaTask).subscribe({
        next: () => {
          this.saving = false;
          this.concluido.emit();
        },
        error: () => {
          this.saving = false;
        },
      });
    }
  }
}