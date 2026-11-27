import { Routes } from '@angular/router';
import { LoginPage } from './pages/login/login';
import { CadastroPage } from './pages/cadastro/cadastro';
import { Dashboard } from './pages/dashboard/dashboard';
import { EntityPage } from './pages/entity-page/entity-page';
import { MovimentacoesPage } from './pages/movimentacoes/movimentacoes';
import { NotFound } from './pages/not-found/not-found';
import { authGuard, guestGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginPage, canActivate: [guestGuard] },
  { path: 'cadastro', component: CadastroPage, canActivate: [guestGuard] },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },

  // As telas de registros usam a mesma EntityPage
  { path: 'tarefas', component: EntityPage, canActivate: [authGuard], data: { entity: 'tarefas' } },
  { path: 'produtos', component: EntityPage, canActivate: [authGuard], data: { entity: 'produtos' } },
  { path: 'orcamentos', component: EntityPage, canActivate: [authGuard], data: { entity: 'orcamentos' } },
  { path: 'usuarios', component: EntityPage, canActivate: [authGuard], data: { entity: 'usuarios' } },
  { path: 'movimentacoes', component: MovimentacoesPage, canActivate: [authGuard] },

  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: '**', component: NotFound },
];
