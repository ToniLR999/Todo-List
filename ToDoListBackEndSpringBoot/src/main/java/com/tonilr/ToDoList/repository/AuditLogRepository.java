package com.tonilr.ToDoList.repository;

import com.tonilr.ToDoList.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Elimina logs más antiguos que la fecha especificada
     */
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    int deleteLogsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Cuenta logs más antiguos que la fecha especificada
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    long countLogsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Cuenta el total de logs en la base de datos
     */
    @Query("SELECT COUNT(a) FROM AuditLog a")
    long countTotalLogs();
}
