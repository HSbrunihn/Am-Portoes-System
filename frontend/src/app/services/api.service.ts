import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ApiEntity,
  ApiPayload,
  StatusTarefa,
  Tarefa,
} from '../models/api.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  list<T extends ApiEntity>(resource: string): Observable<T[]> {
    return this.http.get<T[]>(`${environment.apiUrl}/${resource}`);
  }

  get<T extends ApiEntity>(resource: string, id: number): Observable<T> {
    return this.http.get<T>(`${environment.apiUrl}/${resource}/${id}`);
  }

  create<T extends ApiEntity>(resource: string, payload: ApiPayload): Observable<T> {
    return this.http.post<T>(`${environment.apiUrl}/${resource}`, payload);
  }

  update<T extends ApiEntity>(
    resource: string,
    id: number,
    payload: ApiPayload,
  ): Observable<T> {
    return this.http.put<T>(`${environment.apiUrl}/${resource}/${id}`, payload);
  }

  /** PATCH de status — body em JSON string para o Spring aceitar o enum */
  updateTaskStatus(id: number, status: StatusTarefa): Observable<Tarefa> {
    return this.http.patch<Tarefa>(
      `${environment.apiUrl}/tarefas/${id}/status`,
      JSON.stringify(status),
      { headers: { 'Content-Type': 'application/json' } },
    );
  }

  delete(resource: string, id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/${resource}/${id}`);
  }
}