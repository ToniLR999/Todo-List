import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, Subject, of, BehaviorSubject } from 'rxjs';
import { TaskList } from '../models/task-list.model';
import { tap, catchError, map } from 'rxjs/operators';
import { CacheService } from './cache.service';
import { PaginationService } from './pagination.service';
import { environment } from '../../environments/environment';

export interface PaginatedTaskListsResponse {
  content: TaskList[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class TaskListService {
  private apiUrl = `${environment.apiUrl}/api/lists`;
  public listUpdated = new Subject<void>();
  private readonly CACHE_KEY_PREFIX = 'task_lists_';
  private readonly CACHE_TTL = 2 * 60 * 1000; // 2 minutos
  
  // Subject para notificar cambios en las listas
  private taskListsSubject = new BehaviorSubject<TaskList[]>([]);
  public taskLists$ = this.taskListsSubject.asObservable();

  constructor(
    private http: HttpClient,
    private cacheService: CacheService,
    private paginationService: PaginationService
  ) {}

  /**
   * Creates HTTP headers with JWT authorization token.
   * @returns HttpHeaders with Bearer token
   */
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  /**
   * Retrieves all task lists with pagination and caching support.
   * @returns Observable of TaskList array
   */
  getTaskLists(): Observable<TaskList[]> {
    // Si ya tenemos datos en el observable, devolverlos inmediatamente
    const currentValue = this.taskListsSubject.value;
    if (currentValue.length > 0) {
      return of(currentValue);
    }

    const paginationParams = this.paginationService.getPaginationParams();
    const cacheKey = this.cacheService.generateKey(
      this.CACHE_KEY_PREFIX + 'all',
      paginationParams
    );
    
    const cachedData = this.cacheService.get<TaskList[]>(cacheKey);
    if (cachedData) {
      this.taskListsSubject.next(cachedData);
      return of(cachedData);
    }

    const params = new HttpParams()
      .set('page', paginationParams.page.toString())
      .set('size', paginationParams.size.toString());

    return this.http.get<PaginatedTaskListsResponse>(`${this.apiUrl}`, { params }).pipe(
      map(response => {
        // Actualizar paginación
        this.paginationService.updatePaginationState({
          totalItems: response.totalElements,
          totalPages: response.totalPages
        });
        
        const taskLists = response.content;
        this.cacheService.set(cacheKey, taskLists, this.CACHE_TTL);
        this.taskListsSubject.next(taskLists);
        return taskLists;
      }),
      catchError(error => {
        console.error('❌ Error al obtener listas de tareas:', error);
        console.error('❌ Status:', error.status);
        console.error('❌ Message:', error.message);
        return of([]);
      })
    );
  }

  /**
   * Precarga las listas de tareas para mejorar el rendimiento
   * @returns Observable of TaskList array
   */
  preloadTaskLists(): Observable<TaskList[]> {
    return this.getTaskLists();
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
      tap(newTaskList => {
        // Invalidar cache relacionado
        this.invalidateRelatedCache();
        // Agregar nueva lista al subject
        const currentLists = this.taskListsSubject.value;
        this.taskListsSubject.next([...currentLists, newTaskList]);
        this.listUpdated.next();
      }),
      catchError(error => {
        console.error('Error al crear lista de tareas:', error);
        return of({} as TaskList);
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
        // Invalidar cache relacionado
        this.invalidateRelatedCache();
        // Remover lista del subject
        const currentLists = this.taskListsSubject.value;
        const filteredLists = currentLists.filter(list => list.id !== id);
        this.taskListsSubject.next(filteredLists);
        this.listUpdated.next();
      }),
      catchError(error => {
        console.error('Error al eliminar lista de tareas:', error);
        return of(void 0);
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
      tap(updatedTaskList => {
        // Invalidar cache relacionado
        this.invalidateRelatedCache();
        // Actualizar lista en el subject
        const currentLists = this.taskListsSubject.value;
        const updatedLists = currentLists.map(list => 
          list.id === id ? { ...list, ...updatedTaskList } : list
        );
        this.taskListsSubject.next(updatedLists);
        this.listUpdated.next();
      }),
      catchError(error => {
        console.error('Error al actualizar lista de tareas:', error);
        return of({} as TaskList);
      })
    );
  }

  /**
   * Retrieves a specific task list by ID with caching.
   * @param id Task list ID
   * @returns Observable of TaskList
   */
  getTaskListById(id: number): Observable<TaskList> {
    const cacheKey = this.CACHE_KEY_PREFIX + `id_${id}`;
    const cachedData = this.cacheService.get<TaskList>(cacheKey);
    
    if (cachedData) {
      return of(cachedData);
    }

    return this.http.get<TaskList>(`${this.apiUrl}/${id}`).pipe(
      tap(taskList => {
        this.cacheService.set(cacheKey, taskList, this.CACHE_TTL);
      }),
      catchError(error => {
        console.error('Error al obtener lista de tareas por ID:', error);
        return of({} as TaskList);
      })
    );
  }

  /**
   * Searches task lists by name with pagination and caching.
   * @param name Search term
   * @returns Observable of TaskList array
   */
  searchTaskLists(name: string): Observable<TaskList[]> {
    if (!name.trim()) {
      return this.getTaskLists();
    }

    const paginationParams = this.paginationService.getPaginationParams();
    const cacheKey = this.cacheService.generateKey(
      this.CACHE_KEY_PREFIX + `search_${name}`,
      { ...paginationParams, name }
    );
    
    const cachedData = this.cacheService.get<TaskList[]>(cacheKey);
    if (cachedData) {
      return of(cachedData);
    }

    const params = new HttpParams()
      .set('name', name)
      .set('page', paginationParams.page.toString())
      .set('size', paginationParams.size.toString());

    return this.http.get<PaginatedTaskListsResponse>(`${this.apiUrl}/search`, { params }).pipe(
      map(response => {
        // Actualizar paginación
        this.paginationService.updatePaginationState({
          totalItems: response.totalElements,
          totalPages: response.totalPages
        });
        
        const taskLists = response.content;
        this.cacheService.set(cacheKey, taskLists, this.CACHE_TTL);
        return taskLists;
      }),
      catchError(error => {
        console.error('Error al buscar listas de tareas:', error);
        return of([]);
      })
    );
  }

  /**
   * Invalidates all task list-related cache entries.
   */
  private invalidateRelatedCache(): void {
    const cacheKeys = this.cacheService.getStats().keys;
    cacheKeys.forEach(key => {
      if (key.startsWith(this.CACHE_KEY_PREFIX)) {
        this.cacheService.delete(key);
      }
    });
  }

  /**
   * Clears all cached data.
   */
  clearCache(): void {
    this.invalidateRelatedCache();
  }

  /**
   * Refreshes the current task lists.
   */
  refreshTaskLists(): void {
    this.invalidateRelatedCache();
    this.getTaskLists().subscribe();
  }
}
