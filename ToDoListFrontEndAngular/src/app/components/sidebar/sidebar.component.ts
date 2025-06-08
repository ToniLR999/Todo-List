import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { TaskListService } from '../../services/task-list.service';
import { TaskList } from '../../models/task-list.model';

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
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.loadTaskLists();
    this.route.params.subscribe(params => {
      this.selectedListId = params['id'] ? Number(params['id']) : null;
    });
  }

  loadTaskLists() {
    this.taskListService.getTaskLists().subscribe({
      next: (lists) => this.taskLists = lists,
      error: (error) => console.error('Error loading lists:', error)
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

  createNewList() {
    this.router.navigate(['/lists/new']);
  }
}
