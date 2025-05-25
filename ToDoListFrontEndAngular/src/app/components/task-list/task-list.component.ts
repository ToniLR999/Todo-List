import { Component, OnInit } from '@angular/core';
import { TaskService } from '../../services/task.service';
import { Task } from '../../models/task.model';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth-service.service';
import { FormBuilder, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';

interface TaskInput {
  priority: 1 | 2 | 3;  // En lugar de 'HIGH' | 'MEDIUM' | 'LOW'
  title: string;
  description?: string;
  dueDate?: string;
}

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.css']
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  newTask: Task = {
    title: '',
    description: '',
    priority: 2,
    dueDate: '',
    completed: false
  };
  showCompleted = false;
  errorMessage: string = '';

  taskForm = this.fb.group({
    title: ['', Validators.required],
    description: [''],
    priority: [2], // Por defecto MEDIUM
    dueDate: ['']
  });

  priorities = [
    { value: 1, label: 'Alta' },
    { value: 2, label: 'Media' },
    { value: 3, label: 'Baja' }
  ];

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private authService: AuthService
  ) {
    this.authService.getAuthStatus().subscribe(isAuthenticated => {
      console.log('Estado de autenticación cambiado:', isAuthenticated);
    });
  }

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.taskService.getTasks(this.showCompleted).subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.errorMessage = '';
      },
      error: (error) => {
        console.error('Error loading tasks:', error);
        this.errorMessage = 'Error al cargar las tareas. Por favor, intente nuevamente.';
      }
    });
  }

  onSubmit() {
    if (this.taskForm.valid) {
      const taskData: TaskInput = {
        title: this.taskForm.value.title || '',
        priority: this.taskForm.value.priority as 1 | 2 | 3,
        description: this.taskForm.value.description || undefined,
        dueDate: this.taskForm.value.dueDate || undefined
      };

      this.taskService.createTask(taskData).subscribe({
        next: (response) => {
          // Manejar respuesta exitosa
          this.taskForm.reset({ priority: 2 }); // Resetear form con prioridad por defecto
        },
        error: (error) => {
          console.error('Error al crear la tarea:', error);
        }
      });
    }
  }

  updateTask(task: Task): void {
    const updatedTask = { ...task, completed: !task.completed };
    this.taskService.updateTask(task.id!, updatedTask).subscribe({
      next: (updatedTask) => {
        const index = this.tasks.findIndex(t => t.id === updatedTask.id);
        if (index !== -1) {
          this.tasks[index] = updatedTask;
        }
        this.errorMessage = '';
      },
      error: (error) => {
        console.error('Error updating task:', error);
        this.errorMessage = 'Error al actualizar la tarea. Por favor, intente nuevamente.';
      }
    });
  }

  deleteTask(id: number): void {
    if (confirm('¿Está seguro de que desea eliminar esta tarea?')) {
      this.taskService.deleteTask(id).subscribe({
        next: () => {
          this.tasks = this.tasks.filter(task => task.id !== id);
          this.errorMessage = '';
        },
        error: (error) => {
          console.error('Error deleting task:', error);
          this.errorMessage = 'Error al eliminar la tarea. Por favor, intente nuevamente.';
        }
      });
    }
  }

  toggleShowCompleted(): void {
    this.showCompleted = !this.showCompleted;
    this.loadTasks();
  }

  getPriorityClass(priority: 1 | 2 | 3): string {
    const priorityMap: Record<1 | 2 | 3, string> = {
      1: 'high',
      2: 'medium',
      3: 'low'
    };
    return `priority-${priorityMap[priority]}`;
  }

  logout(): void {
    this.authService.logout();
  }
}