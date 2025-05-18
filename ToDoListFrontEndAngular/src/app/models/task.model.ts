export interface Task {
  id?: number;
  title: string;
  description: string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  dueDate: string;
  completed: boolean;
  createdAt?: string;
  updatedAt?: string;
} 