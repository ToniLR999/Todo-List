import { Component, OnInit } from '@angular/core';
import { TaskService } from '../../services/task.service';
import { Task } from '../../models/task.model';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth-service.service';
import { FormBuilder, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

interface TaskInput {
  priority: 1 | 2 | 3;  // En lugar de 'HIGH' | 'MEDIUM' | 'LOW'
  title: string;
  description?: string;
  dueDate?: string;
}

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
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

    // Configurar el debounce para la búsqueda
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => {
      this.applyFilters();
    });
  }

  ngOnInit(): void {
    this.showCompleted = false;  // Aseguramos que sea false al inicio
    this.loadTasks();
  }

  loadTasks(): void {
    // Si todos los filtros están en 'all', cargar tareas pendientes
    if (this.statusFilter === 'pending' && this.priorityFilter === 'all' && this.dateFilter === 'all') {
      this.showCompleted = false;  // Forzamos que sea false cuando los filtros están en 'all'
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
    } else {
      this.applyFilters();
    }
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
          // Resetear el formulario
          this.taskForm.reset({ priority: 2 });
          
          // Recargar la lista de tareas
          this.loadTasks();
          
          // Opcional: Mostrar mensaje de éxito
          this.showSuccessMessage('Tarea creada exitosamente');
        },
        error: (error) => {
          console.error('Error al crear la tarea:', error);
          // Opcional: Mostrar mensaje de error
          this.showErrorMessage('Error al crear la tarea');
        }
      });
    }
  }

  // Método auxiliar para mostrar mensaje de éxito
  private showSuccessMessage(message: string) {
    // Aquí puedes implementar tu lógica de notificación
    // Por ejemplo, usando un servicio de notificaciones o un toast
    console.log(message);
  }

  // Método auxiliar para mostrar mensaje de error
  private showErrorMessage(message: string) {
    // Aquí puedes implementar tu lógica de notificación
    console.error(message);
  }

  updateTask(task: Task): void {
    const updatedTask = { ...task, completed: !task.completed };
    this.taskService.updateTask(task.id!, updatedTask).subscribe({
      next: (updatedTask) => {
        // Recargar las tareas para actualizar la lista filtrada
        this.loadTasks();
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

  onSearch(): void {
    this.searchSubject.next(this.searchTerm);
  }

  applyFilters(): void {
    if (this.statusFilter === 'all') {
      // Para 'all' necesitamos tanto las completadas como las pendientes
      this.taskService.getTasks(true).subscribe({
        next: (completedTasks) => {
          this.taskService.getTasks(false).subscribe({
            next: (pendingTasks) => {
              // Combinamos ambas listas
              let filteredTasks = [...completedTasks, ...pendingTasks];
              
              // Aplicamos el filtro de búsqueda si hay término de búsqueda
              if (this.searchTerm.trim()) {
                const searchTermLower = this.searchTerm.toLowerCase().trim();
                filteredTasks = filteredTasks.filter(task => 
                  task.title.toLowerCase().includes(searchTermLower) ||
                  (task.description && task.description.toLowerCase().includes(searchTermLower))
                );
              }
              
              // Aplicamos el resto de filtros
              if (this.priorityFilter !== 'all') {
                filteredTasks = filteredTasks.filter(task => 
                  task.priority.toString() === this.priorityFilter
                );
              }

              if (this.dateFilter !== 'all') {
                const now = new Date();
                filteredTasks = filteredTasks.filter(task => {
                  if (!task.dueDate) return false;
                  const dueDate = new Date(task.dueDate);
                  
                  switch (this.dateFilter) {
                    case 'today':
                      return dueDate.toDateString() === now.toDateString();
                    case 'week':
                      const weekFromNow = new Date(now);
                      weekFromNow.setDate(now.getDate() + 7);
                      return dueDate >= now && dueDate <= weekFromNow;
                    case 'month':
                      const monthFromNow = new Date(now);
                      monthFromNow.setMonth(now.getMonth() + 1);
                      return dueDate >= now && dueDate <= monthFromNow;
                    case 'overdue':
                      return dueDate < now;
                    default:
                      return true;
                  }
                });
              }

              this.tasks = filteredTasks;
              this.errorMessage = '';
            }
          });
        }
      });
    } else {
      // Para 'pending' o 'completed' usamos el filtro normal
      this.taskService.getTasks(this.statusFilter === 'completed').subscribe({
        next: (tasks) => {
          let filteredTasks = tasks;
          
          // Aplicamos el filtro de búsqueda si hay término de búsqueda
          if (this.searchTerm.trim()) {
            const searchTermLower = this.searchTerm.toLowerCase().trim();
            filteredTasks = filteredTasks.filter(task => 
              task.title.toLowerCase().includes(searchTermLower) ||
              (task.description && task.description.toLowerCase().includes(searchTermLower))
            );
          }
          
          if (this.priorityFilter !== 'all') {
            filteredTasks = filteredTasks.filter(task => 
              task.priority.toString() === this.priorityFilter
            );
          }

          if (this.dateFilter !== 'all') {
            const now = new Date();
            filteredTasks = filteredTasks.filter(task => {
              if (!task.dueDate) return false;
              const dueDate = new Date(task.dueDate);
              
              switch (this.dateFilter) {
                case 'today':
                  return dueDate.toDateString() === now.toDateString();
                case 'week':
                  const weekFromNow = new Date(now);
                  weekFromNow.setDate(now.getDate() + 7);
                  return dueDate >= now && dueDate <= weekFromNow;
                case 'month':
                  const monthFromNow = new Date(now);
                  monthFromNow.setMonth(now.getMonth() + 1);
                  return dueDate >= now && dueDate <= monthFromNow;
                case 'overdue':
                  return dueDate < now;
                default:
                  return true;
              }
            });
          }

          this.tasks = filteredTasks;
          this.errorMessage = '';
        }
      });
    }
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
  }

  clearStatusFilter(): void {
    this.statusFilter = 'pending';
    this.applyFilters();
  }

  clearPriorityFilter(): void {
    this.priorityFilter = 'all';
    this.applyFilters();
  }

  clearDateFilter(): void {
    this.dateFilter = 'all';
    this.applyFilters();
  }

  clearAllFilters(): void {
    this.searchTerm = '';
    this.statusFilter = 'pending';
    this.priorityFilter = 'all';
    this.dateFilter = 'all';
    this.applyFilters();
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
}