package com.tonilr.ToDoList.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.model.TaskReminder;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskReminderRepository extends JpaRepository<TaskReminder, Long> {
    List<TaskReminder> findByTaskAndIsSentFalse(Task task);
    List<TaskReminder> findByReminderTimeBeforeAndIsSentFalse(LocalDateTime time);
    void deleteByTask(Task task);
}
