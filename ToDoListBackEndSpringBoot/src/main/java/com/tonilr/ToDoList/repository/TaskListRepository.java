package com.tonilr.ToDoList.repository;

import com.tonilr.ToDoList.model.TaskList;
import com.tonilr.ToDoList.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskListRepository extends JpaRepository<TaskList, Long> {
    List<TaskList> findByOwner(User owner);
}
