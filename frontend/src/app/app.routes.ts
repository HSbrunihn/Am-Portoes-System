import { Routes } from '@angular/router';
import { LoginPage } from './pages/login/login';
import { Painel } from './pages/painel/painel';
import { EntityPage } from './pages/entity-page/entity-page';
import { authGuard, guestGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginPage, canActivate: [guestGuard] },
  { path: 'dashboard', component: Painel, canActivate: [authGuard] },

  // As 3 telas de CRUD usam a mesma EntityPage
  { path: 'tarefas', component: EntityPage, canActivate: [authGuard], data: { entity: 'tarefas' } },
  { path: 'produtos', component: EntityPage, canActivate: [authGuard], data: { entity: 'produtos' } },
  { path: 'orcamentos', component: EntityPage, canActivate: [authGuard], data: { entity: 'orcamentos' } },
  { path: 'usuarios', component: EntityPage, canActivate: [authGuard], data: { entity: 'usuarios' } },

  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: '**', redirectTo: 'login' },
];
