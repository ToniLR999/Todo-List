import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TaskList } from '../models/task-list.model';

@Injectable({
  providedIn: 'root'
})
export class TaskListService {
  private apiUrl = 'http://localhost:8080/api/lists';

  constructor(private http: HttpClient) {}

  getTaskLists(): Observable<TaskList[]> {
    return this.http.get<TaskList[]>(this.apiUrl);
  }

  createTaskList(taskList: TaskList): Observable<TaskList> {
    return this.http.post<TaskList>(this.apiUrl, taskList);
  }
}
