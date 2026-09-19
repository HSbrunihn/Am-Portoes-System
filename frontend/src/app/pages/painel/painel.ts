import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TaskCard } from '../../components/task-card/task-card';
import { TaskForm } from '../../components/task-form/task-form';
import { SummaryCard } from '../../components/summary-card/summary-card';
import { TaskDetail } from '../../components/task-detail/task-detail';

import { TaskService } from '../../services/task';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-painel',
  standalone: true,
  imports: [CommonModule, TaskCard, TaskForm, SummaryCard, TaskDetail],
  templateUrl: './painel.html',
  styleUrl: './painel.scss',
})
export class Painel {
  protected readonly taskService = inject(TaskService);

  protected taskEmEdicao: Task | null = null;
  protected filtroAtivo: 'Todas' | 'Alta' | 'Média' | 'Baixa' = 'Todas';
  protected taskEmDetalhe: Task | null = null;

  // Reatividade usando o método do serviço
  readonly tasks = computed(() => this.taskService.getTasks());

  readonly emAndamento = computed(() =>
    this.tasks().filter((t) => t.status === 'Em andamento').length
  );

  readonly concluidas = computed(() =>
    this.tasks().filter((t) => t.status === 'Concluída').length
  );

  readonly aguardando = computed(() =>
    this.tasks().filter((t) => t.status === 'Pendente').length
  );

  readonly altaPrioridade = computed(() =>
    this.tasks().filter((t) => t.prioridade === 'Alta').length
  );

  readonly tasksFiltradas = computed(() => {
    if (this.filtroAtivo === 'Todas') {
      return this.tasks();
    }
    return this.tasks().filter((t) => t.prioridade === this.filtroAtivo);
  });

  protected setFiltro(prioridade: 'Todas' | 'Alta' | 'Média' | 'Baixa'): void {
    this.filtroAtivo = prioridade;
  }

  protected onExcluirTask(id: number): void {
    this.taskService.deleteTask(id).subscribe();
  }

  protected onEditarTask(task: Task): void {
    this.taskEmEdicao = task;
  }

  protected onFormConcluido(): void {
    this.taskEmEdicao = null;
  }

  protected onVerDetalhes(task: Task): void {
    this.taskEmDetalhe = task;
  }

  protected onFecharDetalhes(): void {
    this.taskEmDetalhe = null;
  }
}