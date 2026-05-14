package com.rustam.taskservice.service;

import com.rustam.taskservice.dto.TaskResponseDTO;
import com.rustam.taskservice.exception.TaskNotFoundException;
import com.rustam.taskservice.model.Task;
import com.rustam.taskservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskQueryService {

    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public TaskResponseDTO findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return toDto(task);
    }

    private TaskResponseDTO toDto(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getName(),
                task.getDurationMs(),
                task.getStatus(),
                task.getResult(),
                task.getCreatedAt(),
                task.getStartedAt()
        );
    }
}