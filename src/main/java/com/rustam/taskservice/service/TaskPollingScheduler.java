package com.rustam.taskservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskPollingScheduler {

    private final TaskExecutionService executionService;
    private final TaskWorker taskWorker;
    private final ExecutorService taskExecutor;

    @Scheduled(fixedDelayString = "${app.worker.poll-delay-ms:1000}")
    public void pollAndDispatch() {
        executionService.claimNextTask()
                .ifPresent(taskId -> taskExecutor.submit(() -> taskWorker.execute(taskId)));
    }
}