package com.rustam.taskservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TaskRequestDTO(

        @NotBlank(message = "externalId must not be blank")
        @Size(max = 100)
        String externalId,

        @NotBlank(message = "name must not be blank")
        @Size(max = 255)
        String name,

        @NotNull(message = "durationMs must not be null")
        @Positive(message = "durationMs must be positive")
        Long durationMs
) {}