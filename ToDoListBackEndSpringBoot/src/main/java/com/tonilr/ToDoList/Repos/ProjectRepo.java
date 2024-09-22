package com.tonilr.ToDoList.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tonilr.ToDoList.Entities.Project;

@Repository
public interface ProjectRepo extends JpaRepository<Project,Long>{

}
