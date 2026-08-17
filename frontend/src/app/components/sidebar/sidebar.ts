import { Component, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-sidebar',
  imports: [],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  @Output() navegar = new EventEmitter<string>();

  onClick(event: Event, secao: string): void {
    event.preventDefault();
    this.navegar.emit(secao);
  }
}