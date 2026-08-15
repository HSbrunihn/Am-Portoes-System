import { Component, signal, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TaskCard } from './components/task-card/task-card';
import { TaskForm } from './components/task-form/task-form';
import { SummaryCard } from './components/summary-card/summary-card';
import { Sidebar } from './components/sidebar/sidebar';
import { Header } from './components/header/header';
import { TaskService } from './services/task';
import { Task } from './models/task.model';
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, TaskCard, TaskForm, SummaryCard, Sidebar, Header],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly title = signal('am-portoes-frontend');
  private taskService = inject(TaskService);
  protected readonly tasks = this.taskService.getTasks();
  protected taskEmEdicao: Task | null = null;

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

  protected onExcluirTask(id: number): void {
    this.taskService.deleteTask(id);

  }
 protected onEditarTask(task: Task): void {
  this.taskEmEdicao = task;
}

protected onFormConcluido(): void {
  this.taskEmEdicao = null;
}

}