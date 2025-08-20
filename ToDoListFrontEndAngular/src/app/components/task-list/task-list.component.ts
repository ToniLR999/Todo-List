import { Component, OnInit } from '@angular/core';
import { TaskService, TaskFilters } from '../../services/task.service';
import { Task } from '../../models/task.model';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { FormBuilder, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { ToastrModule } from 'ngx-toastr';
import { TaskListService } from '../../services/task-list.service';
import { TaskList } from '../../models/task-list.model';
import { TaskDetailComponent } from '../task-detail/task-detail.component';


interface TaskInput {
  priority: 1 | 2 | 3;  // En lugar de 'HIGH' | 'MEDIUM' | 'LOW'
  title: string;
  description?: string;
  dueDate?: string;
  completed: boolean;
  taskListId?: number;
}

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    ReactiveFormsModule, 
    RouterModule,
    ToastrModule,
    TaskDetailComponent
  ],
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
  searchTerm: string = '';
  statusFilter: string = 'pending';
  priorityFilter: string = 'all';
  dateFilter: string = 'all';
  private searchSubject = new Subject<string>();
  loading = false;
  sortField: string = 'dueDate';  // Campo por defecto para ordenar
  sortDirection: 'asc' | 'desc' = 'asc';  // Dirección de ordenación
  currentListId: number | null = null;

  // Añadir propiedades para controlar estados de carga
  isLoading: boolean = false;
  isSubmitting: boolean = false;
  isDeleting: boolean = false;

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

  taskLists: TaskList[] = [];
  selectedListId: number | null = null;
  showListForm = false;
  newList = {
    name: '',
    description: ''
  };

  selectedTask: Task | null = null;

  // Propiedades de paginación
  currentPage = 1;
  pageSize = 20;
  totalItems = 0;
  totalPages = 0;
  hasNextPage = false;
  hasPreviousPage = false;

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private authService: AuthService,
    private toastr: ToastrService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.authService.getAuthStatus().subscribe(isAuthenticated => {
    });

    // Configurar el debounce para la búsqueda
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => {
      this.applyFilters();
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.currentListId = params['id'] ? Number(params['id']) : null;
      
      // Aplicar filtro por defecto de "pendientes"
      this.statusFilter = 'pending';
      this.priorityFilter = 'all';
      this.dateFilter = 'all';
      this.searchTerm = '';
      
      // Usar applyFilters en lugar de loadTasks para asegurar consistencia
      this.applyFilters();
    });
  }
  loadTasks(): void {
    this.loading = true;
    
    // Crear filtros con paginación
    const filters: TaskFilters = {
      search: this.searchTerm,
      status: this.statusFilter,
      priority: this.priorityFilter,
      dateFilter: this.dateFilter,
      tasklistId: this.currentListId || undefined
    };

    // Obtener parámetros de paginación
    const params = {
      page: this.currentPage - 1, // Backend usa 0-based indexing
      size: this.pageSize
    };

    // Llamar al servicio con paginación
    this.taskService.getFilteredTasks(filters).subscribe({
      next: (response: any) => {
        if (response && response.content) {
          // Respuesta paginada del backend
          this.tasks = response.content;
          this.updatePaginationState(response.totalElements);
        } else {
          // Respuesta simple (fallback)
          this.tasks = response || [];
          this.updatePaginationState(this.tasks.length);
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar tareas:', error);
        this.tasks = [];
        this.updatePaginationState(0);
        this.loading = false;
        this.toastr.error('Error al cargar las tareas');
      }
    });
  }

  onSubmit() {
    if (this.taskForm.valid) {
      this.isSubmitting = true;
      const taskData: TaskInput = {
        title: this.taskForm.value.title || '',
        priority: this.taskForm.value.priority as 1 | 2 | 3,
        description: this.taskForm.value.description || undefined,
        dueDate: this.taskForm.value.dueDate || undefined,
        completed: false,
        taskListId: this.currentListId || undefined  // Añadimos el taskListId
      };


      this.toastr.info('Creando tarea...', 'Procesando', { timeOut: 1000 });

      this.taskService.createTask(taskData as Task).subscribe({
        next: (response) => {
          this.taskForm.reset({ priority: 2 });
          this.loadTasks();
          this.isSubmitting = false;
          
          this.toastr.success(
            `Tarea "${response.title}" creada exitosamente`,
            'Nueva Tarea',
            {
              timeOut: 3000,
              progressBar: true,
              closeButton: true
            }
          );
        },
        error: (error) => {
          this.isSubmitting = false;
          //console.error('Error al crear la tarea:', error);
          let errorMessage = 'No se pudo crear la tarea.';
          
          if (error.status === 400) {
            errorMessage = 'Por favor, verifica los datos ingresados.';
          } else if (error.status === 401) {
            errorMessage = 'Tu sesión ha expirado. Por favor, vuelve a iniciar sesión.';
          } else if (error.status === 403) {
            errorMessage = 'No tienes permisos para crear tareas.';
          }
          
          this.toastr.error(
            errorMessage,
            'Error',
            {
              timeOut: 5000,
              progressBar: true,
              closeButton: true
            }
          );
        }
      });
    } else {
      // Notificar errores de validación
      if (this.taskForm.get('title')?.errors?.['required']) {
        this.toastr.warning(
          'El título de la tarea es obligatorio',
          'Campo requerido',
          { timeOut: 3000 }
        );
      }
      
      if (this.taskForm.get('priority')?.errors?.['required']) {
        this.toastr.warning(
          'La prioridad de la tarea es obligatoria',
          'Campo requerido',
          { timeOut: 3000 }
        );
      }
    }
  }

  // Método auxiliar para mostrar mensaje de éxito
  private showSuccessMessage(message: string) {
    this.toastr.success(message, 'Éxito');
  }

  // Método auxiliar para mostrar mensaje de error
  private showErrorMessage(message: string) {
    this.toastr.error(message, 'Error');
  }

  updateTask(task: Task): void {
    const updatedTask = { ...task, completed: !task.completed };
    
    this.taskService.updateTask(task.id!, updatedTask).subscribe({
      next: (updatedTask) => {
        this.loadTasks();
        this.errorMessage = ''; 
        
        if (updatedTask.completed) {
          this.toastr.success(
            `¡Excelente! Has completado la tarea "${updatedTask.title}"`,
            '¡Felicidades!',
            {
              timeOut: 4000,
              progressBar: true,
              closeButton: true
            }
          );
        } else {
          this.toastr.info(
            `Tarea "${updatedTask.title}" marcada como pendiente`,
            'Estado actualizado',
            {
              timeOut: 3000,
              progressBar: true,
              closeButton: true
            }
          );
        }
      },
      error: (error) => {
        //console.error('Error updating task:', error);
        let errorMessage = 'Error al actualizar la tarea.';
        
        if (error.status === 401) {
          errorMessage = 'Tu sesión ha expirado. Por favor, vuelve a iniciar sesión.';
        } else if (error.status === 403) {
          errorMessage = 'No tienes permisos para actualizar esta tarea.';
        } else if (error.status === 404) {
          errorMessage = 'La tarea no se encontró. Puede haber sido eliminada.';
        } else if (error.status === 500) {
          errorMessage = 'Error del servidor. Por favor, inténtalo más tarde.';
        }
        
        this.toastr.error(
          errorMessage,
          'Error al actualizar',
          {
            timeOut: 5000,
            progressBar: true,
            closeButton: true
          }
        );
      }
    });
  }

  deleteTask(id: number): void {
    const taskToDelete = this.tasks.find(t => t.id === id);
    if (taskToDelete && confirm(`¿Estás seguro de que deseas eliminar la tarea "${taskToDelete.title}"?`)) {
      this.confirmDeleteTask(id, taskToDelete);
    }
  }

  // Añadir método auxiliar
  private confirmDeleteTask(id: number, taskToDelete: Task): void {
    this.isDeleting = true;
    this.toastr.info('Eliminando tarea...', 'Procesando', { timeOut: 1000 });

    this.taskService.deleteTask(id).subscribe({
      next: () => {
        this.tasks = this.tasks.filter(task => task.id !== id);
        this.isDeleting = false;
        this.toastr.success(
          `Tarea "${taskToDelete.title}" eliminada correctamente`,
          'Tarea Eliminada',
          {
            timeOut: 3000,
            progressBar: true,
            closeButton: true
          }
        );
      },
      error: (error) => {
        this.isDeleting = false;
        this.toastr.error(
          'No se pudo eliminar la tarea. Por favor, intente nuevamente.',
          'Error',
          {
            timeOut: 5000,
            progressBar: true,
            closeButton: true
          }
        );
      }
    });
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

  onSearch(): void {
    this.searchSubject.next(this.searchTerm);
  }

  applyFilters(): void {
    this.isLoading = true;

    const filters: TaskFilters = {
      search: this.searchTerm,
      status: this.statusFilter === 'all' ? undefined : 
              this.statusFilter === 'completed' ? 'true' : 
              this.statusFilter === 'pending' ? 'false' : 'false', // Asegurar que sea 'false' para pending
      priority: this.priorityFilter,
      dateFilter: this.dateFilter,
      tasklistId: this.currentListId || undefined
    };

    this.taskService.getFilteredTasks(filters).subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.errorMessage = '';
        this.checkTasksStatus(tasks);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('🔄 FRONTEND: Error applying filters:', error);
        this.showErrorMessage('Error al aplicar los filtros. Por favor, intente nuevamente.');
        this.isLoading = false;
      }
    });
}

  hasActiveFilters(): boolean {
    return this.searchTerm !== '' || 
          this.statusFilter !== 'pending' || 
          this.priorityFilter !== 'all' || 
          this.dateFilter !== 'all';
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.applyFilters();
    this.toastr.info('Búsqueda limpiada', 'Filtro limpiado', { timeOut: 2000 });
  }

  clearStatusFilter(): void {
    this.statusFilter = 'pending';
    this.applyFilters();
    this.toastr.info('Filtro de estado restablecido', 'Filtro limpiado', { timeOut: 2000 });
  }

  clearPriorityFilter(): void {
    this.priorityFilter = 'all';
    this.applyFilters();
    this.toastr.info('Filtro de prioridad restablecido', 'Filtro limpiado', { timeOut: 2000 });
  }

  clearDateFilter(): void {
    this.dateFilter = 'all';
    this.applyFilters();
    this.toastr.info('Filtro de fecha restablecido', 'Filtro limpiado', { timeOut: 2000 });
  }

  clearAllFilters(): void {
    this.searchTerm = '';
    this.statusFilter = 'pending';
    this.priorityFilter = 'all';
    this.dateFilter = 'all';
    this.applyFilters();
    
    this.toastr.info(
      'Todos los filtros han sido restablecidos',
      'Filtros limpiados',
      { timeOut: 2000 }
    );
  }

  getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      'pending': 'Pendientes',
      'completed': 'Completadas'
    };
    return labels[status] || status;
  }

  getDateFilterLabel(filter: string): string {
    const labels: { [key: string]: string } = {
      'today': 'Hoy',
      'week': 'Esta semana',
      'month': 'Este mes',
      'overdue': 'Vencidas'
    };
    return labels[filter] || filter;
  }

  getPriorityLabel(priority: string): string {
    const labels: { [key: string]: string } = {
      '1': 'Alta',
      '2': 'Media',
      '3': 'Baja'
    };
    return labels[priority] || priority;
  }

  // Método para cambiar el campo de ordenación
  changeSort(field: string): void {
    if (this.sortField === field) {
      // Si ya estamos ordenando por este campo, cambiamos la dirección
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      // Si es un campo nuevo, lo establecemos y la dirección por defecto es ascendente
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.applyFilters();
  }

  // Método para ordenar las tareas
  private sortTasks(tasks: Task[]): Task[] {
    return tasks.sort((a, b) => {
      let comparison = 0;
      
      switch (this.sortField) {
        case 'dueDate':
          // Para fechas, manejamos el caso de fechas nulas
          if (!a.dueDate && !b.dueDate) return 0;
          if (!a.dueDate) return 1;
          if (!b.dueDate) return -1;
          comparison = new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime();
          break;
          
        case 'priority':
          // Para prioridad, 1 es alta, 2 media, 3 baja
          comparison = a.priority - b.priority;
          break;
          
        case 'title':
          // Para títulos, ordenación alfabética
          comparison = a.title.localeCompare(b.title);
          break;
          
        case 'createdAt':
          // Para fecha de creación, manejamos el caso de fechas nulas
          if (!a.createdAt && !b.createdAt) return 0;
          if (!a.createdAt) return 1;
          if (!b.createdAt) return -1;
          comparison = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
          break;
          
        default:
          comparison = 0;
      }
      
      // Aplicamos la dirección de ordenación
      return this.sortDirection === 'asc' ? comparison : -comparison;
    });
  }

  // Método para verificar tareas próximas y vencidas
  private checkTasksStatus(tasks: Task[]): void {
    const now = new Date();
    const oneDayFromNow = new Date(now.getTime() + 24 * 60 * 60 * 1000);
    let hasOverdueTasks = false;
    let hasUpcomingTasks = false;

    tasks.forEach(task => {
      if (!task.completed && task.dueDate) {
        const dueDate = new Date(task.dueDate);
        
        if (dueDate < now) {
          hasOverdueTasks = true;
        } else if (dueDate <= oneDayFromNow) {
          hasUpcomingTasks = true;
        }
      }
    });

    // Notificar tareas vencidas
    if (hasOverdueTasks) {
      this.toastr.error(
        'Tienes tareas vencidas. Por favor, revísalas.',
        'Tareas Vencidas',
        {
          timeOut: 6000,
          progressBar: true,
          closeButton: true
        }
      );
    }

    // Notificar tareas próximas
    if (hasUpcomingTasks) {
      this.toastr.warning(
        'Tienes tareas que vencen en las próximas 24 horas.',
        'Tareas Próximas',
        {
          timeOut: 5000,
          progressBar: true,
          closeButton: true
        }
      );
    }
  }

  // Añadir método de prueba
  testToastr(): void {
    this.toastr.success('Mensaje de éxito', 'Éxito');
    this.toastr.error('Mensaje de error', 'Error');
    this.toastr.warning('Mensaje de advertencia', 'Advertencia');
    this.toastr.info('Mensaje informativo', 'Info');
  }

  navigateToTaskDetails(taskId: number) {
    this.router.navigate(['/tasks', taskId]);
  }

  openTaskDetail(task: Task) {
    this.selectedTask = task;
  }

  closeTaskDetail() {
    this.selectedTask = null;
  }

  saveTask(updatedTask: Task) {
    this.taskService.updateTask(updatedTask.id!, updatedTask).subscribe({
      next: (response) => {
        const index = this.tasks.findIndex(t => t.id === response.id);
        if (index !== -1) {
          this.tasks[index] = response;
        }
        this.selectedTask = null;
        this.toastr.success('Tarea actualizada correctamente');
      },
      error: (error) => {
        //console.error('Error al actualizar la tarea:', error);
        this.toastr.error('Error al actualizar la tarea');
      }
    });
  }

  // Métodos de paginación
  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadTasks();
    }
  }

  nextPage(): void {
    if (this.hasNextPage) {
      this.goToPage(this.currentPage + 1);
    }
  }

  previousPage(): void {
    if (this.hasPreviousPage) {
      this.goToPage(this.currentPage - 1);
    }
  }

  firstPage(): void {
    this.goToPage(1);
  }

  lastPage(): void {
    this.goToPage(this.totalPages);
  }

  onPageSizeChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const newPageSize = parseInt(select.value, 10);
    this.pageSize = newPageSize;
    this.currentPage = 1; // Reset a la primera página
    this.loadTasks();
  }

  updatePaginationState(totalItems: number): void {
    this.totalItems = totalItems;
    this.totalPages = Math.ceil(totalItems / this.pageSize);
    this.hasNextPage = this.currentPage < this.totalPages;
    this.hasPreviousPage = this.currentPage > 1;
  }

  getDisplayRange(): { start: number; end: number } {
    const start = (this.currentPage - 1) * this.pageSize + 1;
    const end = Math.min(this.currentPage * this.pageSize, this.totalItems);
    return { start, end };
  }

  getPageNumbers(maxVisible: number = 5): number[] {
    const pages: number[] = [];
    
    if (this.totalPages <= maxVisible) {
      // Mostrar todas las páginas si hay pocas
      for (let i = 1; i <= this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Mostrar páginas alrededor de la actual
      const halfVisible = Math.floor(maxVisible / 2);
      let start = Math.max(1, this.currentPage - halfVisible);
      let end = Math.min(this.totalPages, start + maxVisible - 1);
      
      // Ajustar si estamos cerca del final
      if (end - start + 1 < maxVisible) {
        start = Math.max(1, end - maxVisible + 1);
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
    }
    
    return pages;
  }
}