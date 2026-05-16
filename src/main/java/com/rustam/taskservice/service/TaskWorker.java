package com.rustam.taskservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskWorker {

    private final TaskStatusUpdater statusUpdater;

    public void execute(Long taskId) {
        log.info("Executing task id={}", taskId);
        try {
            Long duration = statusUpdater.getDuration(taskId);
            Thread.sleep(duration);
            statusUpdater.markCompleted(taskId, "OK");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            statusUpdater.markFailed(taskId, "Interrupted: " + e.getMessage());
        } catch (Exception e) {
            log.error("Task id={} failed", taskId, e);
            statusUpdater.markFailed(taskId, e.getMessage());
        }
    }
}