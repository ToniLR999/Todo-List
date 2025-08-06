/**
 * Task list service for managing task list operations with caching support.
 * Provides CRUD operations for task lists and cache management
 * with integration to the Spring Boot backend API.
 */
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, Subject, of } from 'rxjs';
import { TaskList } from '../models/task-list.model';
import { tap, catchError } from 'rxjs/operators';
import { CacheService } from './cache.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TaskListService {
  private apiUrl = `${environment.apiUrl}/api/lists`;
  public listUpdated = new Subject<void>();
  private readonly CACHE_KEY = 'task_lists';
  private readonly CACHE_TTL = 2 * 60 * 1000; // 2 minutos

  constructor(
    private http: HttpClient,
    private cacheService: CacheService
  ) {}

  /**
   * Retrieves all task lists with caching support.
   * @returns Observable of TaskList array
   */
  getTaskLists(): Observable<TaskList[]> {
    // Intentar obtener del caché primero
    const cachedData = this.cacheService.get<TaskList[]>(this.CACHE_KEY);
    // console.log('🔍 Buscando en caché:', this.CACHE_KEY, 'Resultado:', cachedData);
    
    if (cachedData) {
      // console.log('✅ Usando caché para listas de tareas');
      return of(cachedData);
    }

    // console.log('🌐 Obteniendo listas desde servidor');
    // Si no está en caché, obtener del servidor
    return this.http.get<TaskList[]>(`${this.apiUrl}`).pipe(
      tap(data => {
        // console.log(' Guardando en caché:', data.length, 'listas');
        // Guardar en caché
        this.cacheService.set(this.CACHE_KEY, data, this.CACHE_TTL);
      }),
      catchError(error => {
        console.error('Error al obtener listas de tareas:', error);
        return of([]);
      })
    );
  }

  /**
   * Creates a new task list and invalidates cache.
   * @param taskList Task list data to create
   * @returns Observable of created TaskList
   */
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

  /**
   * Deletes a task list and invalidates cache.
   * @param id Task list ID to delete
   * @returns Observable of void
   */
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

  /**
   * Updates an existing task list and invalidates cache.
   * @param id Task list ID to update
   * @param taskList Updated task list data
   * @returns Observable of updated TaskList
   */
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

  /**
   * Gets observable for list update notifications.
   * @returns Observable for list update events
   */
  onListUpdated() {
    return this.listUpdated.asObservable();
  }

  /**
   * Manually clears the task list cache.
   */
  clearCache(): void {
    this.cacheService.remove(this.CACHE_KEY);
  }

  /**
   * Creates HTTP headers with JWT authorization token.
   * @returns HttpHeaders with Bearer token
   */
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }
}
