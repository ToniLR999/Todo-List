import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TaskList } from 'src/app/models/task-list.model';
import { TaskListService } from 'src/app/services/task-list.service';
import {   ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

@Component({
  selector: 'app-task-list-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './task-list-list.component.html',
  styleUrl: './task-list-list.component.css'
})
export class TaskListListComponent implements OnInit {
  taskLists: TaskList[] = [];
  isCreatingList = false;
  listForm: FormGroup;

  constructor(
    private taskListService: TaskListService,
    private fb: FormBuilder,
    private toastr: ToastrService,
    private router: Router
  ) {
    this.listForm = this.fb.group({
      name: ['', Validators.required],
      description: ['']
    });
  }

  ngOnInit() {
    this.loadTaskLists();
  }

  loadTaskLists() {
    this.taskListService.getTaskLists().subscribe({
      next: (lists) => this.taskLists = lists,
      error: (error) => this.toastr.error('Error al cargar las listas')
    });
  }

  showNewListForm() {
    this.isCreatingList = true;
  }

  cancelCreate() {
    this.isCreatingList = false;
    this.listForm.reset();
  }

  createList() {
    if (this.listForm.valid) {
      this.taskListService.createTaskList(this.listForm.value).subscribe({
        next: (list) => {
          this.taskLists.push(list);
          this.isCreatingList = false;
          this.listForm.reset();
          this.toastr.success('Lista creada exitosamente');
        },
        error: (error) => this.toastr.error('Error al crear la lista')
      });
    }
  }

  selectList(list: TaskList) {
    this.router.navigate(['/tasks', list.id]);
  }

  editList(list: TaskList) {
    // Implementar edición
  }

  deleteList(list: TaskList) {
    // Implementar eliminación
  }
}
