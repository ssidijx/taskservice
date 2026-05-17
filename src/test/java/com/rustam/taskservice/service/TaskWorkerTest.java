package com.rustam.taskservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskWorkerTest {

    @Mock
    private TaskStatusUpdater statusUpdater;

    @InjectMocks
    private TaskWorker taskWorker;

    @Test
    void shouldExecuteTaskAndMarkCompleted() {
        // given
        Long taskId = 42L;
        when(statusUpdater.getDuration(taskId)).thenReturn(10L);

        // when
        taskWorker.execute(taskId);

        // then
        verify(statusUpdater).getDuration(taskId);
        verify(statusUpdater).markCompleted(taskId, "OK");
        verify(statusUpdater, never()).markFailed(anyLong(), anyString());
    }

    @Test
    void shouldMarkFailedWhenDurationLookupThrows() {
        // given
        Long taskId = 99L;
        when(statusUpdater.getDuration(taskId))
                .thenThrow(new RuntimeException("DB error"));

        // when
        taskWorker.execute(taskId);

        // then
        verify(statusUpdater).markFailed(eq(taskId), contains("DB error"));
        verify(statusUpdater, never()).markCompleted(anyLong(), anyString());
    }
}