/**
 * Navigation component for the main application header.
 * Provides user authentication status, task list navigation, sidebar toggle,
 * and responsive design support for mobile devices.
 */
import { Component, OnInit, HostListener, Output, EventEmitter, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { TaskListService } from '../../services/task-list.service';
import { TaskList } from '../../models/task-list.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-nav',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './nav.component.html',
  styleUrls: ['./nav.component.css']
})
export class NavComponent implements OnInit, OnDestroy {
  @Output() abrirSidebarEvent = new EventEmitter<void>();
  @Output() cerrarSidebarEvent = new EventEmitter<void>();
  username: string = '';
  dropdownOpen = false;
  taskLists: TaskList[] = [];
  listsDropdownOpen = false;
  isMobile: boolean = window.innerWidth <= 768;
  sidebarVisible = false;
  isUserMenuOpen = false;

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService, 
    private router: Router,
    private taskListService: TaskListService,
    private cdr: ChangeDetectorRef
  ) {}

  /**
   * Initializes the component by loading user data and task lists.
   */
  ngOnInit() {
    this.username = this.authService.getUsername() || 'Usuario';
    this.loadTaskLists();
  }

  /**
   * Loads user's task lists for navigation dropdown.
   */
  loadTaskLists() {
    this.taskListService.getTaskLists().pipe(takeUntil(this.destroy$)).subscribe({
      next: (taskLists) => { this.taskLists = taskLists; },
      error: (error) => { console.error('Error al obtener listas de tareas:', error); }
    });
  }

  /**
   * Toggles the user dropdown menu.
   */
  toggleDropdown() {
    this.dropdownOpen = !this.dropdownOpen;
  }

  /**
   * Closes the user dropdown menu with a small delay.
   */
  closeDropdown() {
    setTimeout(() => this.dropdownOpen = false, 150);
  }

  /**
   * Navigates to notifications page and closes dropdown.
   */
  goToNotifications() {
    this.router.navigate(['/notifications']);
    this.dropdownOpen = false;
  }

  /**
   * Logs out the current user and closes dropdown.
   */
  logout() {
    this.authService.logout();
    this.dropdownOpen = false;
  }

  /**
   * Toggles the task lists dropdown menu.
   */
  toggleListsDropdown() {
    this.listsDropdownOpen = !this.listsDropdownOpen;
  }

  /**
   * Navigates to a specific task list or all tasks.
   * @param listId Task list ID to navigate to, or null for all tasks
   */
  selectList(listId: number | null) {
    if (listId) {
      this.router.navigate(['/tasks', listId]);
    } else {
      this.router.navigate(['/tasks']);
    }
    this.listsDropdownOpen = false;
  }

  /**
   * Toggles the sidebar visibility and emits events.
   */
  toggleSidebar() {
    this.sidebarVisible = !this.sidebarVisible;
    if (this.sidebarVisible) {
      this.abrirSidebarEvent.emit();
    } else {
      this.cerrarSidebarEvent.emit();
    }
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

  toggleUserMenu(): void {
    this.isUserMenuOpen = !this.isUserMenuOpen;
    
    this.cdr.detectChanges();
    
    setTimeout(() => {
      const dropdown = document.querySelector('.user-dropdown');
      if (dropdown) {

      }
    }, 50);
  }

  closeUserMenu(): void {
    this.isUserMenuOpen = false;
  }

  // Cerrar menú cuando se hace clic fuera
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    
    // Si el click es en el menú de usuario, no hacer nada
    if (target.closest('.user-menu')) {
      return;
    }
    
    // Si el menú está abierto y el click es fuera, cerrarlo
    if (this.isUserMenuOpen) {
      this.isUserMenuOpen = false;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}