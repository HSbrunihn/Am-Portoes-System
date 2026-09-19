import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-summary-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './summary-card.html',
  styleUrl: './summary-card.scss',
})
export class SummaryCard {
  @Input({ required: true }) titulo!: string;
  @Input({ required: true }) valor!: number;
  @Input() cor: 'primary' | 'success' | 'warning' | 'danger' = 'primary';
}