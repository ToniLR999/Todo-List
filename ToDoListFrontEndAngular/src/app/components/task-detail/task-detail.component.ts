import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TaskService } from '../../services/task.service';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './task-detail.component.html',
  styleUrls: ['./task-detail.component.css']
})
export class TaskDetailComponent implements OnInit {
  task: Task | null = null;
  errorMessage: string = '';
  loading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private taskService: TaskService
  ) {}

  ngOnInit(): void {
    const taskId = this.route.snapshot.paramMap.get('id');
    if (taskId) {
      this.loadTaskDetails(Number(taskId));
    }
  }

  loadTaskDetails(taskId: number): void {
    this.loading = true;
    this.taskService.getTaskDetails(taskId).subscribe({
      next: (task) => {
        this.task = task;
        this.errorMessage = '';
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading task details:', error);
        this.errorMessage = 'Error al cargar los detalles de la tarea';
        this.loading = false;
      }
    });
  }

  getPriorityLabel(priority: number): string {
    const priorities = {
      1: 'Alta',
      2: 'Media',
      3: 'Baja'
    };
    return priorities[priority as keyof typeof priorities] || 'No especificada';
  }

  getPriorityClass(priority: number): string {
    const priorityMap: Record<number, string> = {
      1: 'high',
      2: 'medium',
      3: 'low'
    };
    return `priority-${priorityMap[priority]}`;
  }

  toggleComplete(): void {
    if (this.task) {
      this.taskService.updateTask(this.task.id!, { ...this.task, completed: !this.task.completed })
        .subscribe({
          next: (updatedTask) => {
            this.task = updatedTask;
            this.errorMessage = '';
          },
          error: (error) => {
            console.error('Error updating task:', error);
            this.errorMessage = 'Error al actualizar la tarea';
          }
        });
    }
  }

  deleteTask(): void {
    if (this.task && confirm('¿Está seguro de que desea eliminar esta tarea?')) {
      this.taskService.deleteTask(this.task.id!).subscribe({
        next: () => {
          this.router.navigate(['/tasks']);
        },
        error: (error) => {
          console.error('Error deleting task:', error);
          this.errorMessage = 'Error al eliminar la tarea';
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/tasks']);
  }
}
