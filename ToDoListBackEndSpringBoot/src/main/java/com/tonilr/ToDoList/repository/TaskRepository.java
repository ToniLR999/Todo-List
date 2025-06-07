package com.tonilr.ToDoList.repository;

import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedTo(User user);
    List<Task> findByAssignedToAndPriority(User user, int priority);
    List<Task> findByAssignedToAndDueDateBefore(User user, LocalDateTime dueDate);
    List<Task> findByAssignedToAndTitleContainingIgnoreCase(User user, String title);
    List<Task> findByTaskListId(Long taskListId);
    List<Task> findPendingTasksByUser(User user);

    @Query("SELECT t FROM Task t WHERE t.dueDate <= :reminderTime AND t.assignedTo = :user AND t.completed = false")
    List<Task> findTasksDueBefore(@Param("reminderTime") LocalDateTime reminderTime, @Param("user") User user); 

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user AND t.completed = :completed")
    List<Task> findByAssignedToAndCompleted(@Param("user") User user, @Param("completed") boolean completed);

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user " +
           "AND (:completed IS NULL OR t.completed = :completed) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:dateFilter IS NULL OR t.dueDate = :dateFilter)")
    List<Task> findByAssignedToAndFilters(
        @Param("user") User user,
        @Param("completed") Boolean completed,
        @Param("priority") String priority,
        @Param("dateFilter") String dateFilter
    );

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user AND t.dueDate BETWEEN :startDate AND :endDate")
    List<Task> findByAssignedToAndDueDateBetween(
        @Param("user") User user,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    List<Task> findByAssignedToAndCompletedFalse(User user);

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user AND t.dueDate BETWEEN :start AND :end AND t.completed = false")
    List<Task> findByAssignedToAndDueDateBetweenAndCompletedFalse(
        @Param("user") User user,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user " +
           "AND t.dueDate < :dateTime " +
           "AND t.completed = false")
    List<Task> findByAssignedToAndDueDateBeforeAndCompletedFalse(
        @Param("user") User user,
        @Param("dateTime") LocalDateTime dateTime
    );
}
