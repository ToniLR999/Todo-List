import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TaskService } from '../../services/task.service';
import { Task } from '../../models/task.model';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { TaskList } from '../../models/task-list.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

/**
 * Component for displaying and editing task details.
 * Provides functionality to view, edit, delete, and manage individual tasks.
 * Can be used both as a modal dialog and as a standalone page.
 */
@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './task-detail.component.html',
  styleUrls: ['./task-detail.component.css'],
  providers: [ToastrService, TaskService],
})
export class TaskDetailComponent implements OnInit {
  /** Input task to display/edit */
  @Input() task: Task | null = null;
  
  /** Available task lists for assignment */
  @Input() taskLists: TaskList[] = [];
  
  /** Error message to display */
  errorMessage: string = '';
  
  /** Loading state indicator */
  loading: boolean = true;
  
  /** Flag to control edit mode */
  isEditing = false;
  
  /** Event emitter for saving task changes */
  @Output() save = new EventEmitter<Task>();
  
  /** Event emitter for closing the detail view */
  @Output() close = new EventEmitter<void>();
  
  /** Form data for editing task */
  taskForm: Task = {
    title: '',
    description: '',
    priority: 2,
    dueDate: '',
    completed: false
  };

  private destroy$ = new Subject<void>();

  /**
   * Constructor for TaskDetailComponent.
   * @param route Angular route service for accessing route parameters
   * @param router Angular router for navigation
   * @param taskService Service for task operations
   * @param toastr Service for displaying toast notifications
   */
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private taskService: TaskService,
    private toastr: ToastrService
  ) {}

  /**
   * Lifecycle hook that is called after data-bound properties are initialized.
   * Subscribes to route parameters to load task details if task ID is provided.
   */
  ngOnInit(): void {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe((params: any) => {
      const taskId = params['id'];
      if (taskId) {
        this.loadTask(taskId);
      }
    });
  }

  /**
   * Loads a specific task by its ID from the backend.
   * Updates the component state with the loaded task data.
   * @param taskId The ID of the task to load
   */
  loadTask(taskId: string): void {
    this.loading = true;
    this.taskService.getTaskById(parseInt(taskId, 10)).subscribe({
      next: (task) => {
        this.task = task;
        this.taskForm = { ...task };
        this.errorMessage = '';
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Error al cargar la tarea';
        this.loading = false;
        this.toastr.error('Error al cargar la tarea');
      }
    });
  }

  /**
   * Converts priority number to human-readable text.
   * @param priority The priority number (1=High, 2=Medium, 3=Low)
   * @returns Human-readable priority text
   */
  getPriorityText(priority: number | undefined): string {
    if (priority === undefined) return 'No especificada';
    
    switch (priority) {
      case 1: return 'Alta';
      case 2: return 'Media';
      case 3: return 'Baja';
      default: return 'No especificada';
    }
  }

  /**
   * Returns CSS class name based on priority level for styling.
   * @param priority The priority number (1=High, 2=Medium, 3=Low)
   * @returns CSS class name for priority styling
   */
  getPriorityClass(priority: number): string {
    const priorityMap: Record<number, string> = {
      1: 'high',
      2: 'medium',
      3: 'low'
    };
    return `priority-${priorityMap[priority]}`;
  }

  /**
   * Toggles the completion status of the current task.
   * Updates the task in the backend and refreshes the local state.
   */
  toggleComplete(): void {
    if (this.task) {
      this.taskService.updateTask(this.task.id!, { ...this.task, completed: !this.task.completed })
        .subscribe({
          next: (updatedTask) => { this.task = updatedTask; this.errorMessage = ''; },
          error: () => { this.errorMessage = 'Error al actualizar la tarea'; }
        });
    }
  }

  /**
   * Initiates the deletion process for the current task.
   * Shows a confirmation dialog before proceeding with deletion.
   */
  deleteTask() {
    if (this.task && confirm(`¿Estás seguro de que deseas eliminar la tarea "${this.task.title}"?`)) {
      this.confirmDeleteTask();
    }
  }

  /**
   * Confirms and executes the deletion of the current task.
   * Removes the task from the backend and navigates back to task list.
   * Shows success/error notifications.
   */
  private confirmDeleteTask(): void {
    if (this.task) {
      this.taskService.deleteTask(this.task.id!).subscribe({
        next: () => { this.toastr.success(`Tarea "${this.task!.title}" eliminada correctamente`); this.router.navigate(['/tasks']); },
        error: () => { this.toastr.error('Error al eliminar la tarea'); }
      });
    }
  }

  /**
   * Toggles between view and edit modes.
   * When entering edit mode, populates the form with current task data.
   */
  toggleEdit() {
    this.isEditing = !this.isEditing;
    if (this.isEditing && this.task) {
      this.taskForm = { ...this.task };
    }
  }

  /**
   * Saves changes to the current task.
   * Updates the task in the backend and refreshes the local state.
   * Shows success/error notifications.
   * @param task The updated task data to save
   */
  saveTask(task: Task) {
    if (!task.id) return;
    
    this.taskService.updateTask(task.id, task).subscribe({
      next: (response) => { this.task = response; this.isEditing = false; this.toastr.success('Tarea actualizada correctamente'); },
      error: () => { this.toastr.error('Error al actualizar la tarea'); }
    });
  }

  /**
   * Emits close event to notify parent component.
   * Used when the detail view should be closed (e.g., modal dialog).
   */
  closeTaskDetail() { this.close.emit(); }

  /**
   * Navigates back to the task list view.
   * Used when the component is used as a standalone page.
   */
  goBack(): void { this.router.navigate(['/tasks']); }
}
