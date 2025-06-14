import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Task } from '../models/task.model';
import { tap } from 'rxjs/operators';

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
  private apiUrl = 'http://localhost:8080/api/';

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  getTasks(showCompleted: boolean, listId?: number): Observable<Task[]> {
    let params = new HttpParams()
        .set('showCompleted', showCompleted.toString());
    
    if (listId) {
        params = params.set('listId', listId.toString());
    }

    console.log('Frontend - Enviando petición con parámetros:', {
        showCompleted,
        listId
    });

    return this.http.get<Task[]>(`${this.apiUrl}tasks`, {
        headers: this.getHeaders(),
        params: params
    }).pipe(
        tap(tasks => {
            console.log('Frontend - Tareas recibidas:', tasks);
            console.log('Frontend - TaskListIds de las tareas:', tasks.map(t => t.taskListId));
        })
    );
  }

  createTask(task: TaskInput): Observable<any> {
    console.log("Task a guardar: " + task);
    return this.http.post(`${this.apiUrl}tasks`, task, {
      headers: this.getHeaders()
    });
  }

  updateTask(id: string, task: Task) {
    return this.http.put<Task>(`${this.apiUrl}tasks/${id}`, task, {
      headers: this.getHeaders()
    });
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}tasks/${id}`, {
      headers: this.getHeaders()
    });
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
      console.log('Valor de status recibido:', filters.status); // Debug
      params = params.set('completed', filters.status);
    }
    if (filters.priority && filters.priority !== 'all') {
      params = params.set('priority', filters.priority);
    }
    if (filters.dateFilter && filters.dateFilter !== 'all') {
      params = params.set('dateFilter', filters.dateFilter);
    }
    if (filters.tasklistId) {
      console.log('Añadiendo listId a los parámetros:', filters.tasklistId); // Debug
      params = params.set('taskListId', filters.tasklistId.toString());
    }

    console.log('Parámetros finales:', params.toString()); // Debug
    return this.http.get<Task[]>(`${this.apiUrl}tasks/filter`, { 
      params, 
      headers: this.getHeaders() 
    });
  }

  getTaskById(id: string) {
    return this.http.get<Task>(`${this.apiUrl}tasks/${id}`, {
      headers: this.getHeaders()
    });
  }
} 