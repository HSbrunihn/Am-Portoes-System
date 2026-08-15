import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-summary-card',
  imports: [],
  templateUrl: './summary-card.html',
  styleUrl: './summary-card.scss',
})
export class SummaryCard {
  @Input() titulo!: string;
  @Input() quantidade!: number;
  @Input() cor: string = 'primary';
}