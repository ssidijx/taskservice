package com.rustam.taskservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class WorkerConfig {

    @Value("${app.worker.pool-size:4}")
    private int poolSize;

    @Bean(destroyMethod = "shutdown")
    public ExecutorService taskExecutor() {
        return Executors.newFixedThreadPool(poolSize);
    }
}