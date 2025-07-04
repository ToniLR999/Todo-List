/**
 * Sidebar component for task list navigation and management.
 * Provides navigation between task lists, list management operations,
 * and responsive design support for mobile devices.
 */
import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { TaskListService } from '../../services/task-list.service';
import { TaskList } from '../../models/task-list.model';
import { ToastrService } from 'ngx-toastr';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  taskLists: TaskList[] = [];
  selectedListId: number | null = null;
  isMobile: boolean = window.innerWidth <= 768;
  sidebarVisible: boolean = false;

  constructor(
    private taskListService: TaskListService,
    private router: Router,
    private route: ActivatedRoute,
    private toastr: ToastrService
  ) {}

  /**
   * Initializes the component by loading task lists and setting up route monitoring.
   */
  ngOnInit() {
    this.loadTaskLists();
    
    // Suscribirse a los cambios en la ruta
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      const url = this.router.url;
      const match = url.match(/\/tasks\/list\/(\d+)/);
      if (match) {
        this.selectedListId = Number(match[1]);
      } else {
        this.selectedListId = null;
      }
    });

    // Suscribirse a los cambios en las listas
    this.taskListService.listUpdated.subscribe(() => {
      this.loadTaskLists();
    });
  }

  /**
   * Loads user's task lists for sidebar navigation.
   */
  loadTaskLists() {
    this.taskListService.getTaskLists().subscribe({
      next: (taskLists) => {
        // console.log('🔄 sidebar: Obteniendo listas');
        this.taskLists = taskLists;
      },
      error: (error) => {
        console.error('Error al obtener listas de tareas:', error);
        this.toastr.error('Error al cargar las listas');
      }
    });
  }

  /**
   * Selects a task list and navigates to its tasks.
   * @param listId Task list ID to select, or null for all tasks
   */
  selectList(listId: number | null) {
    this.selectedListId = listId;
    // console.log('🔄 sidebar: Seleccionando lista:', listId);
    if (listId) {
      this.router.navigate(['/tasks/list', listId]);
    } else {
      // console.log('🔄 sidebar: Navegando a /tasks');
      this.router.navigate(['/tasks']);
    }
  }

  /**
   * Placeholder method for editing task lists.
   * @param list Task list to edit
   */
  editList(list: TaskList) {
    // Implementar lógica para editar lista
  }

  /**
   * Initiates deletion of a task list with confirmation.
   * @param list Task list to delete
   */
  deleteList(list: TaskList) {
    if (confirm(`¿Estás seguro de que deseas eliminar la lista "${list.name}"? Esta acción eliminará también todas las tareas asociadas.`)) {
      this.confirmDeleteList(list);
    }
  }

  /**
   * Confirms and executes the deletion of a task list.
   * @param list Task list to delete
   */
  private confirmDeleteList(list: TaskList): void {
    this.taskListService.deleteTaskList(list.id!).subscribe({
      next: () => {
        this.taskLists = this.taskLists.filter(l => l.id !== list.id);
        this.toastr.success(`Lista "${list.name}" eliminada correctamente`);
      },
      error: (error) => {
        this.toastr.error('Error al eliminar la lista');
      }
    });
  }

  /**
   * Navigates to the list management page.
   */
  openListManager() {
    this.router.navigate(['/lists/manage']);
  }

  /**
   * Handles window resize events for responsive design.
   * Automatically hides sidebar on desktop view.
   */
  @HostListener('window:resize')
  onResize() {
    this.isMobile = window.innerWidth <= 768;
    if (!this.isMobile) {
      this.sidebarVisible = false;
    }
  }

  /**
   * Toggles the sidebar visibility.
   */
  toggleSidebar() {
    this.sidebarVisible = !this.sidebarVisible;
  }
}
