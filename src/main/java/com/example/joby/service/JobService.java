package com.example.joby.service;

import com.example.joby.kafka.JobEventProducer;
import com.example.joby.model.Job;
import com.example.joby.model.JobStatus;
import com.example.joby.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobEventProducer jobEventProducer;

    public Job enqueue(String type, String payload, int priority) {
        Job job = Job.builder()
                .type(type)
                .payload(payload)
                .status(JobStatus.PENDING)
                .attempts(0)
                .maxAttempts(3)
                .priority(priority)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Job saved = jobRepository.save(job);
        jobEventProducer.publishJobCreated(saved.getId());
        return saved;

    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<Job> getJobsByStatus(JobStatus status) {
        return jobRepository.findByStatus(status);
    }
}
