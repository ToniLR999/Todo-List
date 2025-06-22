import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TaskService } from '../../services/task.service';
import { Task } from '../../models/task.model';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { TaskList } from '../../models/task-list.model';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './task-detail.component.html',
  styleUrls: ['./task-detail.component.css'],
  providers: [ToastrService, TaskService],
})
export class TaskDetailComponent implements OnInit {
  @Input() task: Task | null = null;
  @Input() taskLists: TaskList[] = [];
  errorMessage: string = '';
  loading: boolean = true;
  isEditing = false;
  @Output() save = new EventEmitter<Task>();
  @Output() close = new EventEmitter<void>();
  taskForm: Task = {
    title: '',
    description: '',
    priority: 2,
    dueDate: '',
    completed: false
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private taskService: TaskService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const taskId = params['id'];
      if (taskId) {
        this.loadTask(taskId);
      }
    });
  }

  loadTask(taskId: string): void {
    this.loading = true;
    this.taskService.getTaskById(taskId).subscribe({
      next: (task) => {
        this.task = task;
        this.taskForm = { ...task };
        this.errorMessage = '';
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar la tarea:', error);
        this.errorMessage = 'Error al cargar la tarea';
        this.loading = false;
        this.toastr.error('Error al cargar la tarea');
      }
    });
  }

  getPriorityText(priority: number | undefined): string {
    if (priority === undefined) return 'No especificada';
    
    switch (priority) {
      case 1: return 'Alta';
      case 2: return 'Media';
      case 3: return 'Baja';
      default: return 'No especificada';
    }
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
      this.taskService.updateTask(this.task.id!.toString(), { ...this.task, completed: !this.task.completed })
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

  deleteTask() {
    if (this.task && confirm(`¿Estás seguro de que deseas eliminar la tarea "${this.task.title}"?`)) {
      this.confirmDeleteTask();
    }
  }

  private confirmDeleteTask(): void {
    if (this.task) {
      this.taskService.deleteTask(this.task.id!).subscribe({
        next: () => {
          this.toastr.success(`Tarea "${this.task!.title}" eliminada correctamente`);
          this.router.navigate(['/tasks']);
        },
        error: (error) => {
          this.toastr.error('Error al eliminar la tarea');
        }
      });
    }
  }

  toggleEdit() {
    this.isEditing = !this.isEditing;
    if (this.isEditing && this.task) {
      this.taskForm = { ...this.task };
    }
  }

  saveTask(task: Task) {
    if (!task.id) return;
    
    this.taskService.updateTask(task.id.toString(), task).subscribe({
      next: (response) => {
        this.task = response;
        this.isEditing = false;
        this.toastr.success('Tarea actualizada correctamente');
      },
      error: (error) => {
        console.error('Error al actualizar la tarea:', error);
        this.toastr.error('Error al actualizar la tarea');
      }
    });
  }

  closeTaskDetail() {
    this.close.emit();
  }

  goBack(): void {
    this.router.navigate(['/tasks']);
  }
}
