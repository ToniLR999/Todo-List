import { Component, OnInit, OnDestroy } from '@angular/core';
import { TaskListService } from '../../services/task-list.service';
import { TaskList } from '../../models/task-list.model';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

/**
 * Component for managing and displaying task lists.
 * Provides functionality to create, edit, delete, and view task lists.
 * Users can organize their tasks into different lists for better organization.
 */
@Component({
  selector: 'app-task-list-list',
  templateUrl: './task-list-list.component.html',
  styleUrls: ['./task-list-list.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule]
})
export class TaskListListComponent implements OnInit, OnDestroy {
  /** Array of task lists to display */
  taskLists: TaskList[] = [];
  
  /** Flag to control the visibility of the new list form */
  showNewListForm = false;
  
  /** Object to hold the new task list data being created/edited */
  newList: TaskList = {
    name: '',
    description: ''
  };
  
  /** Reference to the task list being edited (null if creating new) */
  editingList: TaskList | null = null;

  /** Subscription para el observable de listas */
  private subscription: Subscription = new Subscription();
  
  constructor(
    private taskListService: TaskListService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  ngOnInit() {
    // Suscribirse al observable de listas para actualizaciones en tiempo real
    this.subscription.add(
      this.taskListService.taskLists$.subscribe(taskLists => {
        this.taskLists = taskLists;
      })
    );
    
    // Cargar listas iniciales (usará cache si está disponible)
    this.loadTaskLists();
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  /**
   * Loads all task lists for the current user.
   * Subscribes to the task list service to fetch and display task lists.
   */
  loadTaskLists() {
    // El servicio ya maneja el cache y actualiza el observable
    // Solo necesitamos llamar al método para disparar la carga
    this.taskListService.getTaskLists().subscribe({
      error: (error) => {
        console.error('❌ Error en componente al obtener listas:', error);
        this.toastr.error('Error al cargar las listas');
      }
    });
  }

  /**
   * Creates a new task list or updates an existing one.
   * Handles both creation and editing modes based on the editingList state.
   * Shows success/error notifications and updates the UI accordingly.
   */
  createList() {
    if (this.editingList) {
      // Update existing task list
      this.taskListService.updateTaskList(this.editingList.id!, this.newList).subscribe({
        next: (updatedList) => {
          // Update the list in the local array
          const index = this.taskLists.findIndex(l => l.id === updatedList.id);
          if (index !== -1) {
            this.taskLists[index] = updatedList;
          }
          // Reset form and notify other components
          this.showNewListForm = false;
          this.newList = { name: '', description: '' };
          this.editingList = null;
          this.taskListService.listUpdated.next();
          this.toastr.success('Lista actualizada correctamente');
        },
        error: (error) => {
          console.error('Error al actualizar la lista:', error);
          this.toastr.error('Error al actualizar la lista');
        }
      });
    } else {
      // Create new task list
      this.taskListService.createTaskList(this.newList).subscribe({
        next: (list) => {
          // Add new list to the local array
          this.taskLists = [...this.taskLists, list];
          // Reset form and notify other components
          this.showNewListForm = false;
          this.newList = { name: '', description: '' };
          this.taskListService.listUpdated.next();
          this.toastr.success('Lista creada correctamente');
        },
        error: (error) => {
          console.error('Error al crear la lista:', error);
          this.toastr.error('Error al crear la lista');
        }
      });
    }
  }

  /**
   * Initiates editing mode for a specific task list.
   * Populates the form with the existing list data.
   * @param list The task list to edit
   */
  editList(list: TaskList) {
    this.editingList = { ...list };
    this.newList = {
      name: list.name,
      description: list.description || ''
    };
    this.showNewListForm = true;
  }

  /**
   * Initiates the deletion process for a task list.
   * Shows a confirmation dialog before proceeding with deletion.
   * @param list The task list to delete
   */
  deleteList(list: TaskList) {
    if (confirm(`¿Estás seguro de que deseas eliminar la lista "${list.name}"? Esta acción eliminará también todas las tareas asociadas.`)) {
      this.confirmDeleteList(list);
    }
  }

  /**
   * Confirms and executes the deletion of a task list.
   * Removes the list from the local array and shows success/error notifications.
   * @param list The task list to delete
   */
  private confirmDeleteList(list: TaskList): void {
    this.taskListService.deleteTaskList(list.id!).subscribe({
      next: () => {
        // Remove the list from the local array
        this.taskLists = this.taskLists.filter(l => l.id !== list.id);
        this.toastr.success(`Lista "${list.name}" eliminada correctamente`);
      },
      error: (error) => {
        this.toastr.error('Error al eliminar la lista');
      }
    });
  }

  /**
   * Navigates to the task list view to display tasks within the selected list.
   * Notifies other components about the list update.
   * @param list The task list to view
   */
  viewList(list: TaskList) {
    this.router.navigate(['/tasks/list', list.id]).then(() => {
      this.taskListService.listUpdated.next();
    });
  }

  /**
   * Cancels the current form operation (create/edit).
   * Resets the form state and hides the form.
   */
  cancelForm() {
    this.showNewListForm = false;
    this.newList = { name: '', description: '' };
    this.editingList = null;
  }
}
