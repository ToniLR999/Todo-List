package com.tonilr.ToDoList.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tonilr.ToDoList.Entities.Role;

@Repository
public interface RoleRepo extends JpaRepository<Role,Long>{

}
