package com.tonilr.ToDoList.repository;

import com.tonilr.ToDoList.model.TaskList;
import com.tonilr.ToDoList.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskListRepository extends JpaRepository<TaskList, Long> {
    List<TaskList> findByOwner(User owner);
    List<TaskList> findByOwnerAndNameContainingIgnoreCase(User owner, String name);

    @Query("SELECT DISTINCT tl FROM TaskList tl LEFT JOIN FETCH tl.tasks WHERE tl.owner.id = :ownerId")
    List<TaskList> findByOwnerIdWithTasks(@Param("ownerId") Long ownerId);
}
