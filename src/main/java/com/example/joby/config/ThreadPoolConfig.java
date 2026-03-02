package com.example.joby.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ExecutorService jobExecutorService() {
        return Executors.newFixedThreadPool(5); // 5 concurrent workers
    }
}