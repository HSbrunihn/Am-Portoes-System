import { Routes } from '@angular/router';
import { LoginPage } from './pages/login/login';
import { Painel } from './pages/painel/painel';
import { EntityPage } from './pages/entity-page/entity-page';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  { path: 'dashboard', component: Painel },
  { path: 'tarefas', component: EntityPage, data: { entity: 'tarefas' } },
  { path: 'produtos', component: EntityPage, data: { entity: 'produtos' } },
  { path: 'orcamentos', component: EntityPage, data: { entity: 'orcamentos' } },
  { path: 'usuarios', component: EntityPage, data: { entity: 'usuarios' } },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: '**', redirectTo: 'login' },
];