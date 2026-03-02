package com.example.joby.kafka;

import com.example.joby.repository.JobRepository;
import com.example.joby.model.Job;
import com.example.joby.model.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobEventConsumer {

    private final JobRepository jobRepository;

    @KafkaListener(topics = "job-created", groupId = "joby-workers")
    public void onJobCreated(String jobId) {
        log.info("Received job-created event for jobId={}", jobId);

        jobRepository.findById(Long.parseLong(jobId)).ifPresent(job -> {
            if (job.getStatus() != JobStatus.PENDING) {
                log.warn("Job id={} is not PENDING, skipping", jobId);
                return;
            }

            int claimed = jobRepository.claimJob(job.getId(), LocalDateTime.now());
            if (claimed == 0) {
                log.warn("Job id={} already claimed, skipping", jobId);
                return;
            }

            processJob(job);
        });
    }

    private void processJob(Job job) {
        log.info("Processing job id={} type={}", job.getId(), job.getType());

        job.setStatus(JobStatus.RUNNING);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        try {
            switch (job.getType()) {
                case "EMAIL_NOTIFICATION" -> {
                    log.info("Sending email with payload: {}", job.getPayload());
                    Thread.sleep(1000);
                }
                case "REPORT_GENERATION" -> {
                    log.info("Generating report with payload: {}", job.getPayload());
                    Thread.sleep(3000);
                }
                case "DATA_CLEANUP" -> {
                    log.info("Running cleanup with payload: {}", job.getPayload());
                    Thread.sleep(500);
                }
                default -> throw new IllegalArgumentException("Unknown job type: " + job.getType());
            }

            job.setStatus(JobStatus.DONE);
            log.info("Job id={} completed", job.getId());

        } catch (Exception e) {
            job.setAttempts(job.getAttempts() + 1);
            job.setErrorMessage(e.getMessage());

            if (job.getAttempts() >= job.getMaxAttempts()) {
                job.setStatus(JobStatus.DEAD);
                log.error("Job id={} is DEAD", job.getId());
            } else {
                job.setStatus(JobStatus.PENDING);
                job.setScheduledAt(LocalDateTime.now().plusSeconds(10));
                log.warn("Job id={} requeued for retry", job.getId());
            }
        }

        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);
    }
}