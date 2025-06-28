/**
 * Task service for managing task operations with caching support.
 * Provides CRUD operations for tasks, filtering, searching, and cache management
 * with integration to the Spring Boot backend API.
 */
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Task } from '../models/task.model';
import { tap, catchError } from 'rxjs/operators';
import { CacheService } from './cache.service';

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
  tasklistId?: number;  // Añadimos el listId
}

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiUrl = 'http://localhost:8080/api/tasks';
  private readonly CACHE_KEY_PREFIX = 'tasks_';
  private readonly CACHE_TTL = 2 * 60 * 1000; // 2 minutos

  constructor(
    private http: HttpClient,
    private cacheService: CacheService
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
   * Retrieves all tasks with caching support.
   * @returns Observable of Task array
   */
  getTasks(): Observable<Task[]> {
    const cacheKey = this.CACHE_KEY_PREFIX + 'all';
    const cachedData = this.cacheService.get<Task[]>(cacheKey);
    
    if (cachedData) {
      // console.log('TAREAS DESDE CACHÉ (all)');
      return of(cachedData);
    }
    // console.log('TAREAS DESDE SERVIDOR (all)');

    return this.http.get<Task[]>(`${this.apiUrl}`).pipe(
      tap(data => {
        this.cacheService.set(cacheKey, data, this.CACHE_TTL);
      }),
      catchError(error => {
        console.error('Error al obtener tareas:', error);
        return of([]);
      })
    );
  }

  /**
   * Retrieves tasks for a specific list with caching support.
   * @param listId ID of the task list
   * @returns Observable of Task array
   */
  getTasksByList(listId: number): Observable<Task[]> {
    const cacheKey = this.CACHE_KEY_PREFIX + `list_${listId}`;
    const cachedData = this.cacheService.get<Task[]>(cacheKey);
    
    if (cachedData) {
      // console.log(`TAREAS DESDE CACHÉ (list ${listId})`);
      return of(cachedData);
    }
    // console.log(`TAREAS DESDE SERVIDOR (list ${listId})`);

    return this.http.get<Task[]>(`${this.apiUrl}/list/${listId}`).pipe(
      tap(data => {
        this.cacheService.set(cacheKey, data, this.CACHE_TTL);
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
  createTask(task: Task): Observable<Task> {
    return this.http.post<Task>(`${this.apiUrl}`, task, {
      headers: this.getHeaders()
    }).pipe(
      tap(() => {
        // Invalidar cachés relacionados
        this.invalidateTaskCaches();
      })
    );
  }

  /**
   * Updates an existing task and invalidates related caches.
   * @param id Task ID to update
   * @param task Updated task data
   * @returns Observable of updated Task
   */
  updateTask(id: number, task: Task): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, task, {
      headers: this.getHeaders()
    }).pipe(
      tap(() => {
        // Invalidar cachés relacionados
        this.invalidateTaskCaches();
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
        // Invalidar cachés relacionados
        this.invalidateTaskCaches();
      })
    );
  }

  /**
   * Retrieves tasks by priority level.
   * @param priority Priority level (1, 2, or 3)
   * @returns Observable of Task array
   */
  getTasksByPriority(priority: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/priority/${priority}`, {
      headers: this.getHeaders()
    });
  }

  /**
   * Retrieves tasks by due date.
   * @param date Due date filter
   * @returns Observable of Task array
   */
  getTasksByDueDate(date: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/due-date/${date}`, {
      headers: this.getHeaders()
    });
  }

  /**
   * Searches tasks by title.
   * @param title Search term for task title
   * @returns Observable of Task array
   */
  searchTasks(title: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/search?title=${title}`, {
      headers: this.getHeaders()
    });
  }

  /**
   * Retrieves detailed information for a specific task.
   * @param taskId Task ID to get details for
   * @returns Observable of Task
   */
  getTaskDetails(taskId: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${taskId}`, {
      headers: this.getHeaders()
    });
  }

  /**
   * Retrieves filtered tasks based on multiple criteria.
   * @param filters Task filtering criteria
   * @returns Observable of Task array
   */
  getFilteredTasks(filters: TaskFilters): Observable<Task[]> {
    let params = new HttpParams();
    
    if (filters.search) {
      params = params.set('search', filters.search);
    }
    if (filters.status) {
      params = params.set('completed', filters.status);
    }
    if (filters.priority && filters.priority !== 'all') {
      params = params.set('priority', filters.priority);
    }
    if (filters.dateFilter && filters.dateFilter !== 'all') {
      params = params.set('dateFilter', filters.dateFilter);
    }
    if (filters.tasklistId) {
      params = params.set('taskListId', filters.tasklistId.toString());
    }

    return this.http.get<Task[]>(`${this.apiUrl}/filter`, { 
      params, 
      headers: this.getHeaders() 
    });
  }

  /**
   * Retrieves a task by its ID.
   * @param id Task ID as string
   * @returns Observable of Task
   */
  getTaskById(id: string) {
    return this.http.get<Task>(`${this.apiUrl}/${id}`, {
      headers: this.getHeaders()
    });
  }

  /**
   * Invalidates all task-related caches in localStorage.
   */
  private invalidateTaskCaches(): void {
    // Limpiar todos los cachés de tareas
    const keys = Object.keys(localStorage);
    keys.forEach(key => {
      if (key.includes('todolist_tasks_')) {
        localStorage.removeItem(key);
      }
    });
  }
} 