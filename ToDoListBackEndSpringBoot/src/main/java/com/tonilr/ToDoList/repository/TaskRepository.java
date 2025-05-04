package com.tonilr.ToDoList.repository;

import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedTo(User user);
    List<Task> findByAssignedToAndCompleted(User user, boolean completed);
    List<Task> findByTaskListId(Long taskListId);
}
