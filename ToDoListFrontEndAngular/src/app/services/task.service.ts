import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Task } from '../models/task.model';

interface TaskInput {
  priority: 1 | 2 | 3;
  title: string;
  description?: string;
  dueDate?: string;
}

interface TaskFilters {
  search?: string;
  status?: string;
  priority?: string;
  dateFilter?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiUrl = 'http://localhost:8080/api/tasks';

  constructor(private http: HttpClient) { }

  getTasks(showCompleted: boolean = false): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}?completed=${showCompleted}`);
  }

  createTask(task: TaskInput): Observable<any> {
    return this.http.post(`${this.apiUrl}`, task);
  }

  updateTask(id: number, task: Task): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, task);
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getTasksByPriority(priority: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/priority/${priority}`);
  }

  getTasksByDueDate(date: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/due-date/${date}`);
  }

  searchTasks(title: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/search?title=${title}`);
  }

  getTaskDetails(taskId: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${taskId}`);
  }

  getFilteredTasks(filters: TaskFilters): Observable<Task[]> {
    let url = `${this.apiUrl}/filter?`;
    const params = new HttpParams();

    if (filters.search) {
      params.set('search', filters.search);
    }
    if (filters.status && filters.status !== 'all') {
      params.set('status', filters.status);
    }
    if (filters.priority && filters.priority !== 'all') {
      params.set('priority', filters.priority);
    }
    if (filters.dateFilter && filters.dateFilter !== 'all') {
      params.set('dateFilter', filters.dateFilter);
    }

    return this.http.get<Task[]>(url, { params });
  }
} 