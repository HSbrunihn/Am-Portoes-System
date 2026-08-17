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

  deleteTask(id: number): void {
    const index = this.tasks.findIndex(task => task.id === id);
    if (index !== -1) {
      this.tasks.splice(index, 1);
    }
  }

  updateTask(taskAtualizada: Task): void {
    const index = this.tasks.findIndex(task => task.id === taskAtualizada.id);
    if (index !== -1) {
      this.tasks[index] = taskAtualizada;
    }
  }
}