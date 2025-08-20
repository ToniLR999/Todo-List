import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, of, BehaviorSubject } from 'rxjs';
import { Task } from '../models/task.model';
import { tap, catchError, map, switchMap } from 'rxjs/operators';
import { CacheService } from './cache.service';
import { PaginationService, PaginationState } from './pagination.service';
import { DebounceService } from './debounce.service';
import { environment } from '../../environments/environment';

interface TaskInput {
  priority: 1 | 2 | 3;
  title: string;
  description?: string;
  dueDate?: string;
}

export interface TaskFilters {
  search?: string;
  status?: string;
  priority?: string;
  dateFilter?: string;
  tasklistId?: number;
}

export interface PaginatedTasksResponse {
  content: Task[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiUrl = `${environment.apiUrl}/api/tasks`;
  private readonly CACHE_KEY_PREFIX = 'tasks_';
  private readonly CACHE_TTL = 2 * 60 * 1000; // 2 minutos
  
  // Subject para notificar cambios en las tareas
  private tasksSubject = new BehaviorSubject<Task[]>([]);
  public tasks$ = this.tasksSubject.asObservable();

  constructor(
    private http: HttpClient,
    private cacheService: CacheService,
    private paginationService: PaginationService,
    private debounceService: DebounceService
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
   * Retrieves all tasks with pagination and caching support.
   * @returns Observable of Task array
   */
  getTasks(): Observable<Task[]> {
    const paginationParams = this.paginationService.getPaginationParams();
    const cacheKey = this.cacheService.generateKey(
      this.CACHE_KEY_PREFIX + 'all',
      paginationParams
    );
    
    const cachedData = this.cacheService.get<Task[]>(cacheKey);
    if (cachedData) {
      this.tasksSubject.next(cachedData);
      return of(cachedData);
    }

    const params = new HttpParams()
      .set('page', paginationParams.page.toString())
      .set('size', paginationParams.size.toString());

    return this.http.get<PaginatedTasksResponse>(`${this.apiUrl}`, { params }).pipe(
      map(response => {
        // Actualizar paginación
        this.paginationService.updatePaginationState({
          totalItems: response.totalElements,
          totalPages: response.totalPages
        });
        
        const tasks = response.content;
        this.cacheService.set(cacheKey, tasks, this.CACHE_TTL);
        this.tasksSubject.next(tasks);
        return tasks;
      }),
      catchError(error => {
        console.error('Error al obtener tareas:', error);
        return of([]);
      })
    );
  }

  /**
   * Retrieves tasks for a specific list with pagination and caching support.
   * @param listId ID of the task list
   * @returns Observable of Task array
   */
  getTasksByList(listId: number): Observable<Task[]> {
    const paginationParams = this.paginationService.getPaginationParams();
    const cacheKey = this.cacheService.generateKey(
      this.CACHE_KEY_PREFIX + `list_${listId}`,
      { ...paginationParams, listId }
    );
    
    const cachedData = this.cacheService.get<Task[]>(cacheKey);
    if (cachedData) {
      this.tasksSubject.next(cachedData);
      return of(cachedData);
    }

    const params = new HttpParams()
      .set('page', paginationParams.page.toString())
      .set('size', paginationParams.size.toString());

    return this.http.get<PaginatedTasksResponse>(`${this.apiUrl}/list/${listId}`, { params }).pipe(
      map(response => {
        // Actualizar paginación
        this.paginationService.updatePaginationState({
          totalItems: response.totalElements,
          totalPages: response.totalPages
        });
        
        const tasks = response.content;
        this.cacheService.set(cacheKey, tasks, this.CACHE_TTL);
        this.tasksSubject.next(tasks);
        return tasks;
      }),
      catchError(error => {
        console.error('Error al obtener tareas de lista:', error);
        return of([]);
      })
    );
  }

  /**
   * Creates a new task and invalidates related caches.
   * @param task Task data to create
   * @returns Observable of created Task
   */
  createTask(task: TaskInput): Observable<Task> {
    return this.http.post<Task>(`${this.apiUrl}`, task, {
      headers: this.getHeaders()
    }).pipe(
      tap(newTask => {
        // Invalidar cache relacionado
        this.invalidateRelatedCache();
        // Agregar nueva tarea al subject
        const currentTasks = this.tasksSubject.value;
        this.tasksSubject.next([...currentTasks, newTask]);
      }),
      catchError(error => {
        console.error('Error al crear tarea:', error);
        return of({} as Task);
      })
    );
  }

  /**
   * Updates an existing task and invalidates related caches.
   * @param id Task ID
   * @param task Updated task data
   * @returns Observable of updated Task
   */
  updateTask(id: number, task: Partial<Task>): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, task, {
      headers: this.getHeaders()
    }).pipe(
      tap(updatedTask => {
        // Invalidar cache relacionado
        this.invalidateRelatedCache();
        // Actualizar tarea en el subject
        const currentTasks = this.tasksSubject.value;
        const updatedTasks = currentTasks.map(t => 
          t.id === id ? { ...t, ...updatedTask } : t
        );
        this.tasksSubject.next(updatedTasks);
      }),
      catchError(error => {
        console.error('Error al actualizar tarea:', error);
        return of({} as Task);
      })
    );
  }

  /**
   * Deletes a task and invalidates related caches.
   * @param id Task ID to delete
   * @returns Observable of void
   */
  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      headers: this.getHeaders()
    }).pipe(
      tap(() => {
        // Invalidar cache relacionado
        this.invalidateRelatedCache();
        // Remover tarea del subject
        const currentTasks = this.tasksSubject.value;
        const filteredTasks = currentTasks.filter(t => t.id !== id);
        this.tasksSubject.next(filteredTasks);
      }),
      catchError(error => {
        console.error('Error al eliminar tarea:', error);
        return of(void 0);
      })
    );
  }

  /**
   * Retrieves tasks by priority with pagination and caching.
   * @param priority Priority level (1, 2, or 3)
   * @returns Observable of Task array
   */
  getTasksByPriority(priority: string): Observable<Task[]> {
    const paginationParams = this.paginationService.getPaginationParams();
    const cacheKey = this.cacheService.generateKey(
      this.CACHE_KEY_PREFIX + `priority_${priority}`,
      { ...paginationParams, priority }
    );
    
    const cachedData = this.cacheService.get<Task[]>(cacheKey);
    if (cachedData) {
      return of(cachedData);
    }

    const params = new HttpParams()
      .set('page', paginationParams.page.toString())
      .set('size', paginationParams.size.toString());

    return this.http.get<Task[]>(`${this.apiUrl}/priority/${priority}`, { params }).pipe(
      tap(tasks => {
        this.cacheService.set(cacheKey, tasks, this.CACHE_TTL);
      }),
      catchError(error => {
        console.error('Error al obtener tareas por prioridad:', error);
        return of([]);
      })
    );
  }

  /**
   * Retrieves tasks by due date with pagination and caching.
   * @param date Due date string
   * @returns Observable of Task array
   */
  getTasksByDueDate(date: string): Observable<Task[]> {
    const paginationParams = this.paginationService.getPaginationParams();
    const cacheKey = this.cacheService.generateKey(
      this.CACHE_KEY_PREFIX + `due_date_${date}`,
      { ...paginationParams, date }
    );
    
    const cachedData = this.cacheService.get<Task[]>(cacheKey);
    if (cachedData) {
      return of(cachedData);
    }

    const params = new HttpParams()
      .set('page', paginationParams.page.toString())
      .set('size', paginationParams.size.toString());

    return this.http.get<Task[]>(`${this.apiUrl}/due-date/${date}`, { params }).pipe(
      tap(tasks => {
        this.cacheService.set(cacheKey, tasks, this.CACHE_TTL);
      }),
      catchError(error => {
        console.error('Error al obtener tareas por fecha:', error);
        return of([]);
      })
    );
  }

  /**
   * Searches tasks with debounce and pagination.
   * @param title Search term
   * @returns Observable of Task array
   */
  searchTasks(title: string): Observable<Task[]> {
    if (!title.trim()) {
      return this.getTasks();
    }

    const paginationParams = this.paginationService.getPaginationParams();
    const cacheKey = this.cacheService.generateKey(
      this.CACHE_KEY_PREFIX + `search_${title}`,
      { ...paginationParams, title }
    );
    
    const cachedData = this.cacheService.get<Task[]>(cacheKey);
    if (cachedData) {
      return of(cachedData);
    }

    const params = new HttpParams()
      .set('title', title)
      .set('page', paginationParams.page.toString())
      .set('size', paginationParams.size.toString());

    return this.http.get<Task[]>(`${this.apiUrl}/search`, { params }).pipe(
      tap(tasks => {
        this.cacheService.set(cacheKey, tasks, this.CACHE_TTL);
      }),
      catchError(error => {
        console.error('Error al buscar tareas:', error);
        return of([]);
      })
    );
  }

  /**
   * Retrieves a specific task by ID with caching.
   * @param taskId Task ID
   * @returns Observable of Task
   */
  getTaskById(taskId: number): Observable<Task> {
    const cacheKey = this.CACHE_KEY_PREFIX + `id_${taskId}`;
    const cachedData = this.cacheService.get<Task>(cacheKey);
    
    if (cachedData) {
      return of(cachedData);
    }

    return this.http.get<Task>(`${this.apiUrl}/${taskId}`).pipe(
      tap(task => {
        this.cacheService.set(cacheKey, task, this.CACHE_TTL);
      }),
      catchError(error => {
        console.error('Error al obtener tarea por ID:', error);
        return of({} as Task);
      })
    );
  }

  /**
   * Retrieves filtered tasks with pagination and caching.
   * @param filters Task filters
   * @returns Observable of Task array
   */
  getFilteredTasks(filters: TaskFilters): Observable<Task[]> {
    const paginationParams = this.paginationService.getPaginationParams();
    const cacheKey = this.cacheService.generateKey(
      this.CACHE_KEY_PREFIX + 'filtered',
      { ...paginationParams, ...filters }
    );
    
    const cachedData = this.cacheService.get<Task[]>(cacheKey);
    if (cachedData) {
      return of(cachedData);
    }

    let params = new HttpParams()
      .set('page', paginationParams.page.toString())
      .set('size', paginationParams.size.toString());

    // Agregar filtros a los parámetros
    if (filters.search) params = params.set('search', filters.search);
    if (filters.status) params = params.set('status', filters.status);
    if (filters.priority) params = params.set('priority', filters.priority);
    if (filters.dateFilter) params = params.set('dateFilter', filters.dateFilter);
    if (filters.tasklistId) params = params.set('tasklistId', filters.tasklistId.toString());

    return this.http.get<Task[]>(`${this.apiUrl}/filter`, { params }).pipe(
      tap(tasks => {
        this.cacheService.set(cacheKey, tasks, this.CACHE_TTL);
      }),
      catchError(error => {
        console.error('Error al obtener tareas filtradas:', error);
        return of([]);
      })
    );
  }

  /**
   * Creates a search stream with debounce for real-time search.
   * @param delay Debounce delay in milliseconds
   * @returns Observable that emits search results
   */
  createSearchStream(delay: number = 300): Observable<Task[]> {
    return this.debounceService.createSearchStream(delay).pipe(
      switchMap(searchTerm => this.searchTasks(searchTerm))
    );
  }

  /**
   * Creates a filter stream with debounce for real-time filtering.
   * @param delay Debounce delay in milliseconds
   * @returns Observable that emits filtered results
   */
  createFilterStream(delay: number = 500): Observable<Task[]> {
    return this.debounceService.createFilterStream(delay).pipe(
      switchMap(filters => this.getFilteredTasks(filters))
    );
  }

  /**
   * Emits a search term to the search stream.
   * @param searchTerm Term to search for
   */
  emitSearch(searchTerm: string): void {
    this.debounceService.emitSearch(searchTerm);
  }

  /**
   * Emits filter values to the filter stream.
   * @param filters Filter values
   */
  emitFilter(filters: TaskFilters): void {
    this.debounceService.emitFilter(filters);
  }

  /**
   * Invalidates all task-related cache entries.
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
   * Refreshes the current task list.
   */
  refreshTasks(): void {
    this.invalidateRelatedCache();
    this.getTasks().subscribe();
  }
} 