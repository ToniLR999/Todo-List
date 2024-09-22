package com.tonilr.ToDoList.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tonilr.ToDoList.Entities.Tag;

@Repository
public interface TagRepo extends JpaRepository<Tag,Long>{

}
