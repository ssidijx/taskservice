package com.rustam.taskservice.service;

import com.rustam.taskservice.model.Task;
import com.rustam.taskservice.model.TaskStatus;
import com.rustam.taskservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskExecutionService {

    private final TaskRepository taskRepository;

    @Transactional
    public Optional<Long> claimNextTask() {
        return taskRepository.findAndLockFirstByStatus(TaskStatus.NEW)
                .map(task -> {
                    task.setStatus(TaskStatus.IN_PROGRESS);
                    task.setStartedAt(Instant.now());
                    log.info("Claimed task id={}", task.getId());
                    return task.getId();
                });
    }
}