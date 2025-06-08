import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TaskListListComponent } from './task-list-list.component';

describe('TaskListListComponent', () => {
  let component: TaskListListComponent;
  let fixture: ComponentFixture<TaskListListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskListListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TaskListListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
