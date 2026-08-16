import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth.guard';
import { Shell } from './layout/shell';
import { Dashboard } from './pages/dashboard/dashboard';
import { EntityPage } from './pages/entity-page/entity-page';
import { LoginPage } from './pages/login/login';
import { NotFound } from './pages/not-found/not-found';
export const routes: Routes = [
  { path: 'login', component: LoginPage, canActivate: [guestGuard] },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: Dashboard },
      { path: 'tarefas', component: EntityPage, data: { entity: 'tarefas' } },
      { path: 'produtos', component: EntityPage, data: { entity: 'produtos' } },
      { path: 'orcamentos', component: EntityPage, data: { entity: 'orcamentos' } },
      { path: 'usuarios', component: EntityPage, data: { entity: 'usuarios' } },
    ],
  },
  { path: '**', component: NotFound },
];
