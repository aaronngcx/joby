package com.example.joby.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ExecutorService jobExecutorService(
            @Value("${joby.worker.threads:5}") int threads) {
        return Executors.newFixedThreadPool(threads);
    }
}