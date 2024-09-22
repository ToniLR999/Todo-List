package com.tonilr.ToDoList.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tonilr.ToDoList.Entities.TaskHistorical;

@Repository
public interface TaskHistoricalRepo extends JpaRepository<TaskHistorical,Long>{

}
