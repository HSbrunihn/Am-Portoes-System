import { Component, inject, OnInit, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { Orcamento, Produto, Tarefa, Usuario } from '../../models/api.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly totals = signal({
    tarefas: 0,
    produtos: 0,
    orcamentos: 0,
    usuarios: 0,
    pendentes: 0,
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');

    forkJoin({
      tarefas: this.api.list<Tarefa>('tarefas'),
      produtos: this.api.list<Produto>('produtos'),
      orcamentos: this.api.list<Orcamento>('orcamentos'),
      usuarios: this.api.list<Usuario>('usuarios'),
    }).subscribe({
      next: (result) => {
        this.totals.set({
          tarefas: result.tarefas.length,
          produtos: result.produtos.length,
          orcamentos: result.orcamentos.length,
          usuarios: result.usuarios.length,
          pendentes: result.tarefas.filter((task) => task.status === 'PENDENTE').length,
        });
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar o painel. Verifique a conexão com a API.');
        this.loading.set(false);
      },
    });
  }
}
