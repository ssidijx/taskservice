package com.rustam.taskservice.service;

import com.rustam.taskservice.dto.TaskRequestDTO;
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
public class TaskIngestService {

    private final TaskRepository taskRepository;

    @Transactional
    public void ingest(TaskRequestDTO dto) {
        if (taskRepository.existsByExternalId(dto.externalId())) {
            log.info("Task with externalId={} already exists, skipping", dto.externalId());
            log.info("Task with externalId={} already exists, skipping", dto.externalId());
            return;
        }

        Task task = new Task();
        task.setExternalId(dto.externalId());
        task.setName(dto.name());
        task.setDurationMs(dto.durationMs());
        task.setStatus(TaskStatus.NEW);

        Task saved = taskRepository.save(task);
        log.info("Saved task id={} externalId={}", saved.getId(), saved.getExternalId());
    }
}