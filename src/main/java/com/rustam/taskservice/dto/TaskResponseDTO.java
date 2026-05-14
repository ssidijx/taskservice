package com.rustam.taskservice.dto;

import com.rustam.taskservice.model.TaskStatus;

import java.time.Instant;

public record TaskResponseDTO(
        Long id,
        String name,
        Long durationMs,
        TaskStatus status,
        String result,
        Instant createdAt,
        Instant startedAt
) {}