export interface TaskList {
  id?: number;
  name: string;
  description?: string;
  ownerUsername?: string;
  tasks?: Task[];
}
