/**
 * Basic todos component for simple task management.
 * Provides basic CRUD operations for todo items with local state management.
 * This is a simple implementation used for demonstration purposes.
 */
import { Component, OnInit } from '@angular/core';
import { Todo } from 'src/app/models/Todo';

@Component({
  selector: 'app-todos',
  templateUrl: './todos.component.html',
  styleUrls: ['./todos.component.css']
})
export class TodosComponent implements OnInit {

  todos!: Todo[];

  inputTodo:string = "";

  constructor() { }

  /**
   * Initializes the component with sample todo data.
   */
  ngOnInit(): void {
    this.todos = [{

      content: 'First Todo',
      completed: false
    },{
      content: 'Second Todo',
      completed: true
    }]
  }

  /**
   * Toggles the completion status of a todo item.
   * @param id Index of the todo item to toggle
   */
  toggleDone (id: number) {
    this.todos.map((v,i) =>{
      if(i== id) v.completed = !v.completed;
      return v;

    }) 

  }

  /**
   * Deletes a todo item from the list.
   * @param id Index of the todo item to delete
   */
  deleteTodo (id:number){
    this.todos = this.todos.filter((v,i)=> i !== id);

  }

  /**
   * Adds a new todo item to the list.
   * Clears the input field after adding.
   */
  addTodo (){
    this.todos.push({
      content: this.inputTodo,
      completed: false

    })
    this.inputTodo = "";

  }
}
