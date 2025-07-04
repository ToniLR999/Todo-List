/**
 * TaskList interface representing a task list entity in the application.
 * Defines the structure for task list data with optional and required properties.
 * Task lists can contain multiple tasks and belong to a specific user.
 */
export interface TaskList {
  /** Unique identifier for the task list (auto-generated) */
  id?: number;
  /** Name/title of the task list (required) */
  name: string;
  /** Description of the task list */
  description: string;
  /** Username of the task list owner */
  ownerUsername?: string;
  /** Array of tasks belonging to this list */
  tasks?: Task[];
}
