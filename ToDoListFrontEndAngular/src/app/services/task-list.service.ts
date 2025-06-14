import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { TaskList } from '../models/task-list.model';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class TaskListService {
  private apiUrl = 'http://localhost:8080/api/lists';
  public listUpdated = new Subject<void>();

  constructor(private http: HttpClient) {}

  getTaskLists(): Observable<TaskList[]> {
    return this.http.get<TaskList[]>(`${this.apiUrl}`);
  }

  createTaskList(taskList: TaskList): Observable<TaskList> {
    return this.http.post<TaskList>(`${this.apiUrl}`, taskList, {
      headers: this.getHeaders()
    }).pipe(
      tap(() => {
        this.listUpdated.next();
      })
    );
  }

  deleteTaskList(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      headers: this.getHeaders()
    }).pipe(
      tap(() => {
        this.listUpdated.next();
      })
    );
  }

  onListUpdated() {
    return this.listUpdated.asObservable();
  }

  updateTaskList(id: number, taskList: TaskList): Observable<TaskList> {
    console.log('Enviando petición PUT con datos:', taskList);
    const payload = {
      name: taskList.name,
      description: taskList.description,
      id: id
    };
    console.log('Payload final:', payload);
    
    return this.http.put<TaskList>(`${this.apiUrl}/${id}`, payload, {
      headers: this.getHeaders()
    }).pipe(
      tap(response => {
        console.log('Respuesta del servidor:', response);
        this.listUpdated.next();
      })
    );
  }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }
}
