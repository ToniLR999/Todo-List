import { Component, OnInit, HostListener, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { TaskListService } from '../../services/task-list.service';
import { TaskList } from '../../models/task-list.model';

@Component({
  selector: 'app-nav',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './nav.component.html',
  styleUrls: ['./nav.component.css']
})
export class NavComponent implements OnInit {
  @Output() abrirSidebarEvent = new EventEmitter<void>();
  @Output() cerrarSidebarEvent = new EventEmitter<void>();
  username: string = '';
  dropdownOpen = false;
  taskLists: TaskList[] = [];
  listsDropdownOpen = false;
  isMobile: boolean = window.innerWidth <= 768;
  sidebarVisible = false;

  constructor(
    private authService: AuthService, 
    private router: Router,
    private taskListService: TaskListService
  ) {}

  ngOnInit() {
    this.username = this.authService.getUsername() || 'Usuario';
    this.loadTaskLists();
  }

  loadTaskLists() {
    this.taskListService.getTaskLists().subscribe({
      next: (taskLists) => {
        console.log('🔄 nav: Obteniendo listas');
        this.taskLists = taskLists;
      },
      error: (error) => {
        console.error('Error al obtener listas de tareas:', error);
      }
    });
  }

  toggleDropdown() {
    this.dropdownOpen = !this.dropdownOpen;
  }

  closeDropdown() {
    setTimeout(() => this.dropdownOpen = false, 150); // Permite click en opciones
  }

  goToNotifications() {
    this.router.navigate(['/notifications']);
    this.dropdownOpen = false;
  }

  logout() {
    this.authService.logout();
    this.dropdownOpen = false;
  }

  toggleListsDropdown() {
    this.listsDropdownOpen = !this.listsDropdownOpen;
  }

  selectList(listId: number | null) {
    if (listId) {
      this.router.navigate(['/tasks', listId]);
    } else {
      this.router.navigate(['/tasks']);
    }
    this.listsDropdownOpen = false;
  }

  toggleSidebar() {
    this.sidebarVisible = !this.sidebarVisible;
    if (this.sidebarVisible) {
      this.abrirSidebarEvent.emit();
    } else {
      this.cerrarSidebarEvent.emit();
    }
  }

  @HostListener('window:resize')
  onResize() {
    this.isMobile = window.innerWidth <= 768;
    if (!this.isMobile) {
      this.sidebarVisible = false;
    }
  }
}