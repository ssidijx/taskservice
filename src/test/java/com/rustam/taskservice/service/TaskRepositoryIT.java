package com.rustam.taskservice.service;

import com.rustam.taskservice.model.Task;
import com.rustam.taskservice.model.TaskStatus;
import com.rustam.taskservice.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryIT {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    static {
        postgres.start();
    }

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldFindAndLockTaskWithSkipLocked() {
        Task task = new Task();
        task.setExternalId("test-1");
        task.setName("test");
        task.setDurationMs(100L);
        taskRepository.save(task);

        Optional<Task> claimed = taskRepository.findAndLockFirstByStatus(TaskStatus.NEW);

        assertThat(claimed).isPresent();
        assertThat(claimed.get().getExternalId()).isEqualTo("test-1");
    }
}