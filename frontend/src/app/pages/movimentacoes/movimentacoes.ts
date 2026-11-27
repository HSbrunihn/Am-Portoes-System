import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { environment } from '../../../environments/environment';
import { Produto } from '../../models/api.model';

type Tipo = 'ENTRADA' | 'SAIDA';
interface Movimentacao { id: number; produtoNome: string; quantidade: number; tipo: Tipo; observacao: string | null; dataMovimentacao: string; }

// Entrada/saída de estoque. A regra (saldo, estoque insuficiente, transação) é do back-end: POST /api/movimentacoes
@Component({
  selector: 'app-movimentacoes',
  imports: [ReactiveFormsModule],
  template: `
    <section class="page">
      <small>ESTOQUE</small><h1>Movimentações de estoque</h1>
      @if (msg()) { <p class="msg" [class.erro]="erro()">{{ msg() }}</p> }
      <form [formGroup]="form" (ngSubmit)="salvar()">
        <select formControlName="produtoId">
          <option [ngValue]="null" disabled>Produto…</option>
          @for (p of produtos(); track p.id) { <option [ngValue]="p.id">{{ p.nome }} (estoque: {{ p.quantidadeEstoque ?? 0 }})</option> }
        </select>
        <select formControlName="tipo"><option value="ENTRADA">Entrada</option><option value="SAIDA">Saída</option></select>
        <input type="number" formControlName="quantidade" min="1" placeholder="Qtd.">
        <input formControlName="observacao" maxlength="255" placeholder="Observação (opcional)">
        <button [disabled]="salvando()">{{ salvando() ? 'Salvando…' : 'Registrar' }}</button>
      </form>
      <table>
        <tr><th>Data</th><th>Produto</th><th>Tipo</th><th>Qtd.</th><th>Observação</th></tr>
        @for (m of movs(); track m.id) {
          <tr><td>{{ data(m.dataMovimentacao) }}</td><td>{{ m.produtoNome }}</td><td [class]="m.tipo">{{ m.tipo === 'ENTRADA' ? 'Entrada' : 'Saída' }}</td>
            <td>{{ m.quantidade }}</td><td>{{ m.observacao || '—' }}</td></tr>
        } @empty { <tr><td colspan="5">Nenhuma movimentação registrada.</td></tr> }
      </table>
    </section>`,
  styles: [`
    .page{padding:38px;max-width:1100px} small{font-size:11px;font-weight:800;color:#f07812;letter-spacing:1.5px} h1{margin:5px 0 20px;color:#183b56}
    form{display:grid;grid-template-columns:2fr 1fr 110px 2fr auto;gap:10px;background:#fff;border:1px solid #e0e6ea;border-radius:14px;padding:16px}
    select,input,button{padding:10px;border:1px solid #cad5dc;border-radius:8px;font:inherit;min-width:0} button{border:0;background:#ef7d17;color:#fff;font-weight:700;cursor:pointer}
    table{width:100%;margin-top:20px;border-collapse:collapse;background:#fff;border:1px solid #e0e6ea} th,td{text-align:left;padding:12px 16px;border-bottom:1px solid #edf0f2} th{background:#f8fafb;font-size:12px;color:#627686}
    .ENTRADA{color:#227443;font-weight:700} .SAIDA{color:#a22c25;font-weight:700} .msg{padding:12px;border-radius:9px;background:#eaf8ef;color:#227443} .msg.erro{background:#fff0ef;color:#a22c25}
    @media(max-width:800px){form{grid-template-columns:1fr}.page{padding:25px 15px}}`],
})
export class MovimentacoesPage implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/movimentacoes`;
  readonly produtos = signal<Produto[]>([]);
  readonly movs = signal<Movimentacao[]>([]);
  readonly msg = signal('');
  readonly erro = signal(false);
  readonly salvando = signal(false);
  readonly form = new FormGroup({
    produtoId: new FormControl<number | null>(null, Validators.required),
    tipo: new FormControl<Tipo>('ENTRADA', { nonNullable: true }),
    quantidade: new FormControl<number | null>(null, [Validators.required, Validators.min(1), Validators.pattern(/^\d+$/)]),
    observacao: new FormControl('', { nonNullable: true }),
  });

  ngOnInit(): void { this.carregar(); }

  carregar(): void {
    this.http.get<Produto[]>(`${environment.apiUrl}/produtos`).subscribe({ next: (p) => this.produtos.set(p), error: (e) => this.falha(e) });
    this.http.get<Movimentacao[]>(this.url).subscribe({
      next: (m) => this.movs.set([...m].sort((a, b) => b.dataMovimentacao.localeCompare(a.dataMovimentacao))),
      error: (e) => this.falha(e),
    });
  }

  salvar(): void {
    if (this.form.invalid) { this.mostrar('Selecione o produto e informe uma quantidade inteira maior que zero.', true); return; }
    const { observacao, ...dados } = this.form.getRawValue();
    this.salvando.set(true);
    this.http.post(this.url, { ...dados, observacao: observacao.trim() || null }).subscribe({
      next: () => {
        this.salvando.set(false);
        this.mostrar(`${dados.tipo === 'ENTRADA' ? 'Entrada' : 'Saída'} registrada.`, false);
        this.form.patchValue({ quantidade: null, observacao: '' });
        this.carregar();
      },
      error: (e) => { this.salvando.set(false); this.falha(e); },
    });
  }

  data(v: string): string { return new Date(v).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }); }

  // 400 (ex.: "Estoque insuficiente para saída"), 403, 404 e 500 trazem "mensagem" do GlobalExceptionHandler
  private falha(e: HttpErrorResponse): void { this.mostrar(e.status === 0 ? 'Sem conexão com a API.' : e.error?.mensagem ?? 'Não foi possível concluir a operação.', true); }
  private mostrar(texto: string, erro: boolean): void { this.msg.set(texto); this.erro.set(erro); }
}
