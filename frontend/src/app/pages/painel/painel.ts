import { Component, inject } from '@angular/core';
import { TaskCard } from '../../components/task-card/task-card';
import { TaskForm } from '../../components/task-form/task-form';
import { SummaryCard } from '../../components/summary-card/summary-card';
import { Sidebar } from '../../components/sidebar/sidebar';
import { Header } from '../../components/header/header';
import { TaskDetail } from '../../components/task-detail/task-detail';
import { TaskService } from '../../services/task';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-painel',
  imports: [TaskCard, TaskForm, SummaryCard, Sidebar, Header, TaskDetail],
  templateUrl: './painel.html',
  styleUrl: './painel.scss',
})
export class Painel {
  private taskService = inject(TaskService);
  protected readonly tasks = this.taskService.getTasks();
  protected taskEmEdicao: Task | null = null;
  protected filtroAtivo: 'Todas' | 'Alta' | 'Média' | 'Baixa' = 'Todas';
  protected taskEmDetalhe: Task | null = null;

  protected get emAndamento(): number {
    return this.tasks.filter(t => t.status === 'Em andamento').length;
  }

  protected get concluidas(): number {
    return this.tasks.filter(t => t.status === 'Concluída').length;
  }

  protected get aguardando(): number {
    return this.tasks.filter(t => t.status === 'Aguardando').length;
  }

  protected get altaPrioridade(): number {
    return this.tasks.filter(t => t.prioridade === 'Alta').length;
  }

  protected get tasksFiltradas(): Task[] {
    if (this.filtroAtivo === 'Todas') {
      return this.tasks;
    }
    return this.tasks.filter(t => t.prioridade === this.filtroAtivo);
  }

  protected setFiltro(prioridade: 'Todas' | 'Alta' | 'Média' | 'Baixa'): void {
    this.filtroAtivo = prioridade;
  }

  protected onExcluirTask(id: number): void {
    this.taskService.deleteTask(id);
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

  protected onNavegar(secao: string): void {
    if (secao === 'todas') {
      this.filtroAtivo = 'Todas';
    }
  }
}