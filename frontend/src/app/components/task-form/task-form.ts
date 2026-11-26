import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, Output, EventEmitter, inject, OnChanges, SimpleChanges, signal } from '@angular/core';
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
  protected readonly error = signal('');

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
    this.error.set('');

    if (this.taskParaEditar && this.taskParaEditar.id) {
      this.taskService.updateTask(this.taskParaEditar.id, this.novaTask).subscribe({
        next: () => {
          this.saving = false;
          this.concluido.emit();
        },
        error: (error: HttpErrorResponse) => {
          this.handleSaveError(error);
        },
      });
    } else {
      this.taskService.createTask(this.novaTask).subscribe({
        next: () => {
          this.saving = false;
          this.concluido.emit();
        },
        error: (error: HttpErrorResponse) => {
          this.handleSaveError(error);
        },
      });
    }
  }

  private handleSaveError(error: HttpErrorResponse): void {
    this.saving = false;
    const body = error.error as { mensagem?: string; message?: string } | null;
    this.error.set(
      body?.mensagem ?? body?.message ?? 'Não foi possível salvar a tarefa. Tente novamente.',
    );
  }
}
