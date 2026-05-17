package com.rustam.taskservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskWorker {

    private static final String MDC_TASK_ID = "taskId";

    private final TaskStatusUpdater statusUpdater;

    public void execute(Long taskId) {
        MDC.put(MDC_TASK_ID, String.valueOf(taskId));
        try {
            log.info("Executing task");
            Long duration = statusUpdater.getDuration(taskId);
            Thread.sleep(duration);
            statusUpdater.markCompleted(taskId, "OK");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            statusUpdater.markFailed(taskId, "Interrupted: " + e.getMessage());
        } catch (Exception e) {
            log.error("Task failed", e);
            statusUpdater.markFailed(taskId, e.getMessage());
        } finally {
            MDC.remove(MDC_TASK_ID);
        }
    }
}