package com.tonilr.ToDoList.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tonilr.ToDoList.Entities.Users;

@Repository
public interface UsersRepo extends JpaRepository<Users,Long>{

}
