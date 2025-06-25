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

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  getTasks(): Observable<Task[]> {
    const cacheKey = this.CACHE_KEY_PREFIX + 'all';
    const cachedData = this.cacheService.get<Task[]>(cacheKey);
    
    if (cachedData) {
      console.log('TAREAS DESDE CACHÉ (all)');
      return of(cachedData);
    }
    console.log('TAREAS DESDE SERVIDOR (all)');

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

  getTasksByList(listId: number): Observable<Task[]> {
    const cacheKey = this.CACHE_KEY_PREFIX + `list_${listId}`;
    const cachedData = this.cacheService.get<Task[]>(cacheKey);
    
    if (cachedData) {
      console.log(`TAREAS DESDE CACHÉ (list ${listId})`);
      return of(cachedData);
    }
    console.log(`TAREAS DESDE SERVIDOR (list ${listId})`);

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

  getTasksByPriority(priority: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/priority/${priority}`, {
      headers: this.getHeaders()
    });
  }

  getTasksByDueDate(date: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/due-date/${date}`, {
      headers: this.getHeaders()
    });
  }

  searchTasks(title: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/search?title=${title}`, {
      headers: this.getHeaders()
    });
  }

  getTaskDetails(taskId: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${taskId}`, {
      headers: this.getHeaders()
    });
  }

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

  getTaskById(id: string) {
    return this.http.get<Task>(`${this.apiUrl}/${id}`, {
      headers: this.getHeaders()
    });
  }

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