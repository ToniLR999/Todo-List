import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Task } from '../models/task.model';

interface TaskInput {
  priority: 1 | 2 | 3;
  title: string;
  description?: string;
  dueDate?: string;
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
} 