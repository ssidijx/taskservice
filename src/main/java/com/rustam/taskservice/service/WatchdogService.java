package com.rustam.taskservice.service;

import com.rustam.taskservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class WatchdogService {

    private final TaskRepository taskRepository;

    @Value("${app.watchdog.stuck-threshold-ms:30000}")
    private long stuckThresholdMs;

    @Scheduled(fixedDelayString = "${app.watchdog.poll-delay-ms:10000}")
    @Transactional
    public void resetStuckTasks() {
        Instant threshold = Instant.now().minus(Duration.ofMillis(stuckThresholdMs));
        int resetCount = taskRepository.resetStuckTasks(threshold);
        if (resetCount > 0) {
            log.warn("Watchdog reset {} stuck task(s) older than {}", resetCount, threshold);
        }
    }
}