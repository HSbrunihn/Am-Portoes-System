import { Component, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-sidebar',
  imports: [],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  @Output() navegar = new EventEmitter<string>();

  onClick(secao: string): void {
    this.navegar.emit(secao);
  }
  
}