package com.tonilr.ToDoList.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tonilr.ToDoList.Entities.Reminder;

@Repository
public interface ReminderRepo extends JpaRepository<Reminder,Long>{

}
