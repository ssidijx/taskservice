package com.rustam.taskservice.service;

import com.rustam.taskservice.exception.TaskNotFoundException;
import com.rustam.taskservice.model.Task;
import com.rustam.taskservice.model.TaskStatus;
import com.rustam.taskservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskStatusUpdater {

    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public Long getDuration(Long taskId) {
        return taskRepository.findById(taskId)
                .map(Task::getDurationMs)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    @Transactional
    public void markCompleted(Long taskId, String result) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        task.setStatus(TaskStatus.COMPLETED);
        task.setResult(result);
        log.info("Task id={} marked COMPLETED", taskId);
    }

    @Transactional
    public void markFailed(Long taskId, String error) {
        taskRepository.findById(taskId).ifPresent(task -> {
            task.setStatus(TaskStatus.FAILED);
            task.setResult(error);
            log.warn("Task id={} marked FAILED: {}", taskId, error);
        });
    }
}