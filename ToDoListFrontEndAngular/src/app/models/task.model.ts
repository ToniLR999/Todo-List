/**
 * Task interface representing a task entity in the application.
 * Defines the structure for task data with optional and required properties.
 * Priority levels: 1 (High), 2 (Medium), 3 (Low)
 */
export interface Task {
  /** Unique identifier for the task (auto-generated) */
  id?: number;
  /** Title/name of the task (required) */
  title: string;
  /** Detailed description of the task */
  description: string;
  /** Priority level: 1=High, 2=Medium, 3=Low */
  priority: 1 | 2 | 3;
  /** Due date for the task in ISO string format */
  dueDate: string;
  /** Completion status of the task */
  completed: boolean;
  /** Timestamp when the task was created */
  createdAt?: string;
  /** Timestamp when the task was last updated */
  updatedAt?: string;
  /** ID of the task list this task belongs to */
  taskListId?: number;
  /** Name of the task list this task belongs to */
  taskListName?: string;
} 