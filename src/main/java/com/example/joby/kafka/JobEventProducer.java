package com.example.joby.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobEventProducer {

    private static final String TOPIC = "job-created";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publishJobCreated(Long jobId) {
        kafkaTemplate.send(TOPIC, jobId.toString());
        log.info("Published job-created event for jobId={}", jobId);
    }
}