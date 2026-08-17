import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  ApiEntity,
  ApiError,
  OrcamentoRequest,
  ProdutoRequest,
  StatusTarefa,
  TarefaRequest,
  UsuarioRequest,
} from '../../models/api.model';
import { ApiService } from '../../services/api.service';
type Resource = 'tarefas' | 'produtos' | 'orcamentos' | 'usuarios';
type FieldType = 'text' | 'email' | 'password' | 'number' | 'date' | 'textarea';
interface Field {
  key: string;
  label: string;
  type: FieldType;
  required?: boolean;
  min?: number;
}
const LABELS: Record<Resource, string> = {
  tarefas: 'Tarefas',
  produtos: 'Produtos',
  orcamentos: 'Orçamentos',
  usuarios: 'Usuários',
};
const FIELDS: Record<Resource, Field[]> = {
  tarefas: [
    { key: 'titulo', label: 'Título', type: 'text', required: true },
    { key: 'descricao', label: 'Descrição', type: 'textarea' },
    { key: 'dataVencimento', label: 'Data de vencimento', type: 'date', required: true },
  ],
  orcamentos: [
    { key: 'titulo', label: 'Título', type: 'text', required: true },
    { key: 'descricao', label: 'Descrição', type: 'textarea' },
    { key: 'dataVencimento', label: 'Data de vencimento', type: 'date', required: true },
  ],
  produtos: [
    { key: 'codigo', label: 'Código', type: 'number', required: true, min: 0 },
    { key: 'nome', label: 'Nome', type: 'text', required: true },
    { key: 'preco', label: 'Preço', type: 'number', required: true, min: 0.01 },
    { key: 'dataValidade', label: 'Data de validade', type: 'date' },
  ],
  usuarios: [
    { key: 'nome', label: 'Nome', type: 'text', required: true },
    { key: 'email', label: 'E-mail', type: 'email', required: true },
    { key: 'senha', label: 'Senha', type: 'password', required: true, min: 6 },
  ],
};
@Component({
  selector: 'app-entity-page',
  imports: [ReactiveFormsModule],
  templateUrl: './entity-page.html',
  styleUrl: './entity-page.scss',
})
export class EntityPage implements OnInit, OnDestroy {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private sub?: Subscription;
  resource: Resource = 'tarefas';
  title = '';
  fields: Field[] = [];
  form = new FormGroup({});
  readonly items = signal<ApiEntity[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly feedback = signal('');
  readonly search = signal('');
  readonly modal = signal(false);
  readonly detail = signal<ApiEntity | null>(null);
  editingId: number | null = null;
  ngOnInit(): void {
    this.sub = this.route.data.subscribe((data) => {
      this.resource = data['entity'] as Resource;
      this.title = LABELS[this.resource];
      this.fields = FIELDS[this.resource];
      this.buildForm();
      this.load();
    });
  }
  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
  private buildForm(): void {
    const controls: Record<string, FormControl<string | number | null>> = {};
    for (const f of this.fields) {
      const rules = [];
      if (f.required) rules.push(Validators.required);
      if (f.type === 'email') rules.push(Validators.email);
      if (f.key === 'senha') rules.push(Validators.minLength(6));
      if (f.min !== undefined) rules.push(Validators.min(f.min));
      controls[f.key] = new FormControl(null, { validators: rules });
    }
    this.form = new FormGroup(controls);
  }
  control(key: string): AbstractControl | null {
    return this.form.get(key);
  }
  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.list(this.resource).subscribe({
      next: (v) => {
        this.items.set(v);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar os dados. Verifique a conexão com a API.');
        this.loading.set(false);
      },
    });
  }
  filtered(): ApiEntity[] {
    const q = this.search().toLocaleLowerCase('pt-BR').trim();
    return q
      ? this.items().filter((i) => JSON.stringify(i).toLocaleLowerCase('pt-BR').includes(q))
      : this.items();
  }
  newItem(): void {
    this.editingId = null;
    this.form.reset();
    this.modal.set(true);
  }
  edit(item: ApiEntity): void {
    this.editingId = item.id;
    const values: Record<string, string | number | null> = {};
    for (const f of this.fields) {
      values[f.key] = this.value(item, f.key);
      if (f.key === 'senha') values[f.key] = '';
    }
    this.form.reset(values);
    this.modal.set(true);
  }
  view(id: number): void {
    this.api
      .get(this.resource, id)
      .subscribe({
        next: (v) => this.detail.set(v),
        error: () => this.error.set('Não foi possível consultar o registro.'),
      });
  }
  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const v = this.form.getRawValue() as Record<string, string | number | null>;
    let payload: TarefaRequest | OrcamentoRequest | ProdutoRequest | UsuarioRequest;
    if (this.resource === 'produtos')
      payload = {
        codigo: Number(v['codigo']),
        nome: String(v['nome']),
        preco: Number(v['preco']),
        dataValidade: v['dataValidade'] ? String(v['dataValidade']) : null,
      };
    else if (this.resource === 'usuarios')
      payload = { nome: String(v['nome']), email: String(v['email']), senha: String(v['senha']) };
    else
      payload = {
        titulo: String(v['titulo']),
        descricao: String(v['descricao'] ?? ''),
        dataVencimento: String(v['dataVencimento']),
      };
    const request =
      this.editingId === null
        ? this.api.create(this.resource, payload)
        : this.api.update(this.resource, this.editingId, payload);
    request.subscribe({
      next: () => {
        this.saving.set(false);
        this.modal.set(false);
        this.feedback.set(
          this.editingId === null
            ? 'Registro criado com sucesso.'
            : 'Registro atualizado com sucesso.',
        );
        this.load();
      },
      error: (e: HttpErrorResponse) => {
        this.saving.set(false);
        this.error.set(this.errorMessage(e));
      },
    });
  }
  remove(item: ApiEntity): void {
    if (!confirm(`Excluir o registro #${item.id}? Esta ação não pode ser desfeita.`)) return;
    this.api.delete(this.resource, item.id).subscribe({
      next: () => {
        this.feedback.set('Registro excluído com sucesso.');
        this.load();
      },
      error: () => this.error.set('Não foi possível excluir o registro.'),
    });
  }
  changeStatus(item: ApiEntity, event: Event): void {
    if (this.resource !== 'tarefas') return;
    const status = (event.target as HTMLSelectElement).value as StatusTarefa;
    this.api.updateTaskStatus(item.id, status).subscribe({
      next: () => {
        this.feedback.set('Status atualizado.');
        this.load();
      },
      error: () => this.error.set('Não foi possível atualizar o status.'),
    });
  }
  value(item: ApiEntity, key: string): string | number | null {
    const record = item as unknown as Record<string, string | number | null>;
    return record[key] ?? null;
  }
  display(item: ApiEntity, key: string): string {
    const v = this.value(item, key);
    if (v === null || v === '') return '—';
    if (key === 'preco')
      return Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    return String(v);
  }
  statusLabel(v: string | number | null): string {
    return String(v)
      .replace('EM_ANDAMENTO', 'Em andamento')
      .replace('PENDENTE', 'Pendente')
      .replace('CONCLUIDA', 'Concluída');
  }
  private errorMessage(e: HttpErrorResponse): string {
    const body = e.error as ApiError | undefined;
    if (body?.campos) return Object.values(body.campos).join(' ');
    return body?.mensagem ?? 'Não foi possível salvar. Revise os dados e tente novamente.';
  }
}
