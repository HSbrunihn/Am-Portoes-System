import { Routes } from '@angular/router';
import { LoginPage } from './pages/login/login';
import { Painel } from './pages/painel/painel';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  { path: 'dashboard', component: Painel },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: '**', redirectTo: 'login' },
];