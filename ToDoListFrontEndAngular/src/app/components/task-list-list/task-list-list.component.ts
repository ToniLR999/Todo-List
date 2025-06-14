import { Component, OnInit } from '@angular/core';
import { TaskListService } from '../../services/task-list.service';
import { TaskList } from '../../models/task-list.model';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-task-list-list',
  templateUrl: './task-list-list.component.html',
  styleUrls: ['./task-list-list.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule]
})
export class TaskListListComponent implements OnInit {
  taskLists: TaskList[] = [];
  showNewListForm = false;
  newList: TaskList = {
    name: '',
    description: ''
  };
  editingList: TaskList | null = null;

  constructor(
    private taskListService: TaskListService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  ngOnInit() {
    this.loadTaskLists();
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

  createList() {
    if (this.editingList) {
      console.log('Editando lista. Datos a enviar:', this.newList);
      this.taskListService.updateTaskList(this.editingList.id!, this.newList).subscribe({
        next: (updatedList) => {
          console.log('Lista actualizada recibida:', updatedList);
          const index = this.taskLists.findIndex(l => l.id === updatedList.id);
          if (index !== -1) {
            this.taskLists[index] = updatedList;
          }
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
      this.taskListService.createTaskList(this.newList).subscribe({
        next: (list) => {
          this.taskLists = [...this.taskLists, list];
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

  editList(list: TaskList) {
    console.log('Lista a editar:', list);
    this.editingList = { ...list };
    this.newList = {
      name: list.name,
      description: list.description || ''
    };
    console.log('Nueva lista preparada:', this.newList);
    this.showNewListForm = true;
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

  viewList(list: TaskList) {
    this.router.navigate(['/tasks/list', list.id]).then(() => {
      this.taskListService.listUpdated.next();
    });
  }

  cancelForm() {
    this.showNewListForm = false;
    this.newList = { name: '', description: '' };
    this.editingList = null;
  }
}
