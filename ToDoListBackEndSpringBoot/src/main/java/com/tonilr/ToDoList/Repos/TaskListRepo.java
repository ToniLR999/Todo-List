package com.tonilr.ToDoList.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tonilr.ToDoList.Entities.List;
import com.tonilr.ToDoList.Entities.TaskList;

@Repository
public interface TaskListRepo extends JpaRepository<TaskList,Long>{

}
