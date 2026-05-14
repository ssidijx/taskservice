package com.rustam.taskservice.kafka;

import com.rustam.taskservice.dto.TaskRequestDTO;
import com.rustam.taskservice.service.TaskIngestService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskConsumer {

    private final TaskIngestService taskIngestService;
    private final Validator validator;

    @KafkaListener(topics = "${app.kafka.topic.tasks}")
    public void consume(TaskRequestDTO dto) {
        log.info("Received message: {}", dto);

        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining(", "));
            log.warn("Invalid message, skipping: {}", errors);
            return;
        }

        taskIngestService.ingest(dto);
    }
}