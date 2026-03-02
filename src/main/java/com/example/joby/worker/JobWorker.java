package com.example.joby.worker;

import com.example.joby.model.Job;
import com.example.joby.model.JobStatus;
import com.example.joby.repository.JobRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class JobWorker {

    private final JobRepository jobRepository;
    private final ExecutorService jobExecutorService;
    private final Counter jobsCompletedCounter;
    private final Counter jobsFailedCounter;
    private final Counter jobsDeadCounter;
    private final Timer jobProcessingTimer;

    public JobWorker(JobRepository jobRepository, ExecutorService jobExecutorService, MeterRegistry meterRegistry) {
        this.jobRepository = jobRepository;
        this.jobExecutorService = jobExecutorService;
        this.jobsCompletedCounter = Counter.builder("joby.jobs.completed")
                .description("Total jobs completed")
                .register(meterRegistry);
        this.jobsFailedCounter = Counter.builder("joby.jobs.failed")
                .description("Total jobs failed")
                .register(meterRegistry);
        this.jobsDeadCounter = Counter.builder("joby.jobs.dead")
                .description("Total jobs dead")
                .register(meterRegistry);
        this.jobProcessingTimer = Timer.builder("joby.jobs.processing.time")
                .description("Job processing time")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelay = 5000)
    public void processPendingJobs() {
        List<Job> pendingJobs = jobRepository.findPendingJobs(JobStatus.PENDING, LocalDateTime.now());
        for (Job job : pendingJobs) {
            jobExecutorService.submit(() -> processWithLifecycle(job));
        }
    }

    private void processWithLifecycle(Job job) {
        int claimed = jobRepository.claimJob(job.getId(), LocalDateTime.now());
        if (claimed == 0) {
            log.warn("Job id={} already claimed by another thread, skipping", job.getId());
            return;
        }

        log.info("Processing job id={} type={} priority={} thread={}", job.getId(), job.getType(), job.getPriority(), Thread.currentThread().getName());

        job.setStatus(JobStatus.RUNNING);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        jobProcessingTimer.record(() -> {
            try {
                processJob(job);
                job.setStatus(JobStatus.DONE);
                jobsCompletedCounter.increment();
                log.info("Job id={} completed", job.getId());

            } catch (Exception e) {
                job.setAttempts(job.getAttempts() + 1);
                job.setErrorMessage(e.getMessage());
                log.error("Job id={} failed, attempts={}/{}", job.getId(), job.getAttempts(), job.getMaxAttempts());

                if (job.getAttempts() >= job.getMaxAttempts()) {
                    job.setStatus(JobStatus.DEAD);
                    jobsDeadCounter.increment();
                    log.error("Job id={} is DEAD after {} attempts", job.getId(), job.getAttempts());
                } else {
                    job.setStatus(JobStatus.PENDING);
                    job.setScheduledAt(LocalDateTime.now().plusSeconds(10));
                    jobsFailedCounter.increment();
                    log.warn("Job id={} requeued for retry", job.getId());
                }
            }
        });

        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);
    }

    private void processJob(Job job) throws Exception {
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
    }
}
