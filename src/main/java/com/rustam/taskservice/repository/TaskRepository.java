package com.rustam.taskservice.repository;

import com.rustam.taskservice.model.Task;
import com.rustam.taskservice.model.TaskStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    boolean existsByExternalId(String externalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
            SELECT t FROM Task t
            WHERE t.status = :status
            ORDER BY t.id
            LIMIT 1
            """)
    Optional<Task> findAndLockFirstByStatus(@Param("status") TaskStatus status);
}