import { Component, OnInit } from '@angular/core';
import { TaskService } from '../../services/task.service';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-task-list',
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.css']
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  newTask: Task = {
    title: '',
    description: '',
    priority: 'MEDIUM',
    dueDate: '',
    completed: false
  };
  showCompleted = false;
  errorMessage: string = '';

  constructor(private taskService: TaskService) { }

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

  createTask(): void {
    if (!this.newTask.title.trim()) {
      this.errorMessage = 'El título es requerido';
      return;
    }

    this.taskService.createTask(this.newTask).subscribe({
      next: (task) => {
        this.tasks.push(task);
        this.newTask = {
          title: '',
          description: '',
          priority: 'MEDIUM',
          dueDate: '',
          completed: false
        };
        this.errorMessage = '';
      },
      error: (error) => {
        console.error('Error creating task:', error);
        this.errorMessage = 'Error al crear la tarea. Por favor, intente nuevamente.';
      }
    });
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

  getPriorityClass(priority: string): string {
    return `priority-${priority.toLowerCase()}`;
  }
} 