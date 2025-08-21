package com.tonilr.ToDoList.repository;

import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.model.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Finds all tasks assigned to a specific user.
     * @param user The user to find tasks for
     * @return List of tasks assigned to the user
     */
    List<Task> findByAssignedTo(User user);
    
    /**
     * Finds all tasks assigned to a specific user with a given priority level.
     * @param user The user to find tasks for
     * @param priority The priority level to filter by
     * @return List of tasks matching the user and priority criteria
     */
    List<Task> findByAssignedToAndPriority(User user, int priority);
    
    /**
     * Finds all tasks assigned to a user that are due before a specific date/time.
     * @param user The user to find tasks for
     * @param dueDate The cutoff date/time for task due dates
     * @return List of overdue or upcoming tasks for the user
     */
    List<Task> findByAssignedToAndDueDateBefore(User user, LocalDateTime dueDate);
    
    /**
     * Finds tasks assigned to a user with titles containing the specified text (case-insensitive).
     * @param user The user to find tasks for
     * @param title The text to search for in task titles
     * @return List of tasks with matching titles
     */
    List<Task> findByAssignedToAndTitleContainingIgnoreCase(User user, String title);
    
    /**
     * Finds all tasks belonging to a specific task list.
     * @param taskListId The ID of the task list
     * @return List of tasks in the specified task list
     */
    List<Task> findByTaskListId(Long taskListId);
    
    /**
     * Finds all pending (incomplete) tasks assigned to a specific user.
     * @param user The user to find pending tasks for
     * @return List of incomplete tasks for the user
     */
    List<Task> findPendingTasksByUser(User user);

    /**
     * Custom query to find tasks that are due before a reminder time and assigned to a specific user.
     * Used for sending due date reminders.
     * @param reminderTime The cutoff time for reminders
     * @param user The user to find tasks for
     * @return List of tasks due for reminders
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate <= :reminderTime AND t.assignedTo = :user AND t.completed = false")
    List<Task> findTasksDueBefore(@Param("reminderTime") LocalDateTime reminderTime, @Param("user") User user); 

    /**
     * Finds tasks assigned to a user filtered by completion status.
     * @param user The user to find tasks for
     * @param completed The completion status to filter by
     * @return List of tasks matching the completion criteria
     */
    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user AND t.completed = :completed")
    List<Task> findByAssignedToAndCompleted(@Param("user") User user, @Param("completed") boolean completed);

    /**
     * Advanced filtering method that allows multiple optional filters.
     * Filters can be applied for completion status, priority, and due date.
     * @param user The user to find tasks for
     * @param completed Optional completion status filter (null = no filter)
     * @param priority Optional priority filter (null = no filter)
     * @param dateFilter Optional due date filter (null = no filter)
     * @return List of tasks matching the applied filters
     */
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

    /**
     * Finds tasks assigned to a user with due dates within a specific date range.
     * @param user The user to find tasks for
     * @param startDate The start of the date range
     * @param endDate The end of the date range
     * @return List of tasks due within the specified range
     */
    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user AND t.dueDate BETWEEN :startDate AND :endDate")
    List<Task> findByAssignedToAndDueDateBetween(
        @Param("user") User user,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Finds all incomplete tasks assigned to a specific user.
     * @param user The user to find incomplete tasks for
     * @return List of incomplete tasks for the user
     */
    List<Task> findByAssignedToAndCompletedFalse(User user);

    /**
     * Finds incomplete tasks assigned to a user with due dates within a specific range.
     * @param user The user to find tasks for
     * @param start The start of the date range
     * @param end The end of the date range
     * @return List of incomplete tasks due within the specified range
     */
    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user AND t.dueDate BETWEEN :start AND :end AND t.completed = false")
    List<Task> findByAssignedToAndDueDateBetweenAndCompletedFalse(
        @Param("user") User user,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * Finds incomplete tasks assigned to a user that are overdue (due before a specific date/time).
     * @param user The user to find overdue tasks for
     * @param dateTime The cutoff date/time for overdue tasks
     * @return List of overdue incomplete tasks for the user
     */
    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user " +
           "AND t.dueDate < :dateTime " +
           "AND t.completed = false")
    List<Task> findByAssignedToAndDueDateBeforeAndCompletedFalse(
        @Param("user") User user,
        @Param("dateTime") LocalDateTime dateTime
    );

    /**
     * Finds all tasks belonging to a specific task list.
     * @param taskList The task list to find tasks for
     * @return List of tasks in the specified task list
     */
    List<Task> findByTaskList(TaskList taskList);

    // Cache counting methods for performance optimization
    
    /**
     * Counts the total number of tasks assigned to a specific user.
     * Used for cache management and dashboard statistics.
     * @param user The user to count tasks for
     * @return Total number of tasks assigned to the user
     */
    Long countByAssignedTo(User user);
    
    /**
     * Counts tasks assigned to a user filtered by completion status.
     * Used for cache management and dashboard statistics.
     * @param user The user to count tasks for
     * @param completed The completion status to filter by
     * @return Number of tasks matching the completion criteria
     */
    Long countByAssignedToAndCompleted(User user, boolean completed);
    
    /**
     * Counts tasks assigned to a user with a specific priority level.
     * Used for cache management and dashboard statistics.
     * @param user The user to count tasks for
     * @param priority The priority level to filter by
     * @return Number of tasks with the specified priority
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo = :user AND t.priority = :priority")
    Long countByAssignedToAndPriority(@Param("user") User user, @Param("priority") int priority);
    
    /**
     * Counts tasks assigned to a user that are due before a specific date.
     * Used for cache management and dashboard statistics.
     * @param user The user to count tasks for
     * @param dueDate The cutoff date for counting overdue tasks
     * @return Number of tasks due before the specified date
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo = :user AND t.dueDate < :dueDate")
    Long countByAssignedToAndDueDateBefore(@Param("user") User user, @Param("dueDate") LocalDateTime dueDate);

    /**
     * Finds tasks assigned to a user that belong to a specific task list.
     * @param user The user to find tasks for
     * @param taskListId The ID of the task list to filter by
     * @return List of tasks assigned to the user in the specified task list
     */
    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user AND t.taskList.id = :taskListId")
    List<Task> findByAssignedToAndTaskListId(@Param("user") User user, @Param("taskListId") Long taskListId);

    // AÑADIR paginación para consultas grandes
    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user ORDER BY t.dueDate ASC")
    Page<Task> findByAssignedToWithPagination(@Param("user") User user, Pageable pageable);
}
