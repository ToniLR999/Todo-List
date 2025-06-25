import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, Subject, of } from 'rxjs';
import { TaskList } from '../models/task-list.model';
import { tap, catchError } from 'rxjs/operators';
import { CacheService } from './cache.service';

@Injectable({
  providedIn: 'root'
})
export class TaskListService {
  private apiUrl = 'http://localhost:8080/api/lists';
  public listUpdated = new Subject<void>();
  private readonly CACHE_KEY = 'task_lists';
  private readonly CACHE_TTL = 2 * 60 * 1000; // 2 minutos

  constructor(
    private http: HttpClient,
    private cacheService: CacheService
  ) {}

  getTaskLists(): Observable<TaskList[]> {
    // Intentar obtener del caché primero
    const cachedData = this.cacheService.get<TaskList[]>(this.CACHE_KEY);
    console.log('🔍 Buscando en caché:', this.CACHE_KEY, 'Resultado:', cachedData);
    
    if (cachedData) {
      console.log('✅ Usando caché para listas de tareas');
      return of(cachedData);
    }

    console.log('🌐 Obteniendo listas desde servidor');
    // Si no está en caché, obtener del servidor
    return this.http.get<TaskList[]>(`${this.apiUrl}`).pipe(
      tap(data => {
        console.log(' Guardando en caché:', data.length, 'listas');
        // Guardar en caché
        this.cacheService.set(this.CACHE_KEY, data, this.CACHE_TTL);
      }),
      catchError(error => {
        console.error('Error al obtener listas de tareas:', error);
        return of([]);
      })
    );
  }

  createTaskList(taskList: TaskList): Observable<TaskList> {
    return this.http.post<TaskList>(`${this.apiUrl}`, taskList, {
      headers: this.getHeaders()
    }).pipe(
      tap(() => {
        // Invalidar caché al crear nueva lista
        this.cacheService.remove(this.CACHE_KEY);
        this.listUpdated.next();
      })
    );
  }

  deleteTaskList(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      headers: this.getHeaders()
    }).pipe(
      tap(() => {
        // Invalidar caché al eliminar lista
        this.cacheService.remove(this.CACHE_KEY);
        this.listUpdated.next();
      })
    );
  }

  updateTaskList(id: number, taskList: TaskList): Observable<TaskList> {
    const payload = {
      name: taskList.name,
      description: taskList.description,
      id: id
    };
    
    return this.http.put<TaskList>(`${this.apiUrl}/${id}`, payload, {
      headers: this.getHeaders()
    }).pipe(
      tap(response => {
        // Invalidar caché al actualizar lista
        this.cacheService.remove(this.CACHE_KEY);
        this.listUpdated.next();
      })
    );
  }

  onListUpdated() {
    return this.listUpdated.asObservable();
  }

  // Método para limpiar caché manualmente
  clearCache(): void {
    this.cacheService.remove(this.CACHE_KEY);
  }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }
}
