import { Component, OnInit } from '@angular/core';
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

  constructor(
    private taskListService: TaskListService,
    private router: Router,
    private route: ActivatedRoute,
    private toastr: ToastrService
  ) {}

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

  loadTaskLists() {
    this.taskListService.getTaskLists().subscribe({
      next: (lists) => {
        this.taskLists = lists;
      },
      error: (error) => {
        console.error('Error al cargar las listas:', error);
        this.toastr.error('Error al cargar las listas');
      }
    });
  }

  selectList(listId: number | null) {
    this.selectedListId = listId;
    if (listId) {
      this.router.navigate(['/tasks/list', listId]);
    } else {
      this.router.navigate(['/tasks']);
    }
  }


  editList(list: TaskList) {
    // Implementar lógica para editar lista
  }

  deleteList(list: TaskList) {
    if (confirm(`¿Estás seguro de que deseas eliminar la lista "${list.name}"? Esta acción eliminará también todas las tareas asociadas.`)) {
      this.taskListService.deleteTaskList(list.id!).subscribe({
        next: () => {
          this.taskLists = this.taskLists.filter(l => l.id !== list.id);
          this.toastr.success('Lista eliminada correctamente');
        },
        error: (error) => {
          console.error('Error al eliminar la lista:', error);
          this.toastr.error('Error al eliminar la lista');
        }
      });
    }
  }

  openListManager() {
    this.router.navigate(['/lists/manage']);
  }
}
