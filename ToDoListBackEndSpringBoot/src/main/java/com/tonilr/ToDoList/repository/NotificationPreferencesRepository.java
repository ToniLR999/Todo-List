package com.tonilr.ToDoList.repository;

import com.tonilr.ToDoList.model.NotificationPreferences;
import com.tonilr.ToDoList.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, Long> {
    Optional<NotificationPreferences> findByUser(User user);
}
