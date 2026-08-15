import { Injectable } from '@angular/core';
import { Task } from '../models/task.model';
import { TASKS_MOCK } from '../mock-data/tasks.mock';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private tasks: Task[] = TASKS_MOCK;

  getTasks(): Task[] {
    return this.tasks;
  }

  addTask(task: Task): void {
    this.tasks.push(task);
  }
}