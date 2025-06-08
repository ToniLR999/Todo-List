export interface Task {
  id?: number;
  title: string;
  description: string;
  priority: 1 | 2 | 3;
  dueDate: string;
  completed: boolean;
  createdAt?: string;
  updatedAt?: string;
  taskListId?: number;
  taskListName?: string;
} 