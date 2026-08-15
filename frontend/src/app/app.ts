import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TaskCard } from './components/task-card/task-card';
import { TASKS_MOCK } from './mock-data/tasks.mock';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, TaskCard],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly title = signal('am-portoes-frontend');
  protected readonly tasks = TASKS_MOCK;
}