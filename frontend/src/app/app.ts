import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TaskCard } from './components/task-card/task-card';
import { TASKS_MOCK } from './mock-data/tasks.mock';
import { Sidebar } from './components/sidebar/sidebar';
import { Header } from './components/header/header';
import { TaskService } from './services/task';
import { TaskForm } from './components/task-form/task-form';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, TaskCard,Sidebar,Header,TaskForm],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly title = signal('am-portoes-frontend');
  private tasksService = inject(TaskService);
  protected readonly tasks = TASKS_MOCK;
}