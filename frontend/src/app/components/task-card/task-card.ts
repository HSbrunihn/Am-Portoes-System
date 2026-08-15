import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Task } from '../../models/task.model';
@Component({
  selector: 'app-task-card',
  imports: [],
  templateUrl: './task-card.html',
  styleUrl: './task-card.scss',
})
export class TaskCard {

  @Input() task!: Task;
  @Output() excluir = new EventEmitter<number>();
  @Output() editar = new EventEmitter<Task>();


  onExcluir(): void {
    this.excluir.emit(this.task.id)
  }

  onEditar(): void{
    this.editar.emit(this.task);
  }

}
