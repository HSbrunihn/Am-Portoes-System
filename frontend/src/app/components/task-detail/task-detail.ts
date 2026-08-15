import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-task-detail',
  imports: [],
  templateUrl: './task-detail.html',
  styleUrl: './task-detail.scss',
})
export class TaskDetail {
  @Input() task: Task | null = null;
  @Output() fechar = new EventEmitter<void>();

  onFechar(): void {
    this.fechar.emit();
  }
}