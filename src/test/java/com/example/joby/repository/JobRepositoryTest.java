package com.example.joby.repository;

import com.example.joby.model.Job;
import com.example.joby.model.JobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JobRepositoryTest {

    @Autowired
    private JobRepository jobRepository;

    @Test
    void findByStatus_shouldReturnJobsWithMatchingStatus() {
        Job pending = Job.builder()
                .type("EMAIL_NOTIFICATION")
                .payload("hello")
                .status(JobStatus.PENDING)
                .attempts(0)
                .maxAttempts(3)
                .priority(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Job done = Job.builder()
                .type("DATA_CLEANUP")
                .payload("hello")
                .status(JobStatus.DONE)
                .attempts(0)
                .maxAttempts(3)
                .priority(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        jobRepository.save(pending);
        jobRepository.save(done);

        List<Job> result = jobRepository.findByStatus(JobStatus.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("EMAIL_NOTIFICATION");
    }

    @Test
    void findPendingJobs_shouldRespectScheduledAt() {
        Job readyJob = Job.builder()
                .type("EMAIL_NOTIFICATION")
                .payload("hello")
                .status(JobStatus.PENDING)
                .attempts(0)
                .maxAttempts(3)
                .priority(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .scheduledAt(LocalDateTime.now().minusSeconds(10)) // ready
                .build();

        Job notReadyJob = Job.builder()
                .type("REPORT_GENERATION")
                .payload("hello")
                .status(JobStatus.PENDING)
                .attempts(0)
                .maxAttempts(3)
                .priority(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .scheduledAt(LocalDateTime.now().plusSeconds(60)) // not ready yet
                .build();

        jobRepository.save(readyJob);
        jobRepository.save(notReadyJob);

        List<Job> result = jobRepository.findPendingJobs(JobStatus.PENDING, LocalDateTime.now());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("EMAIL_NOTIFICATION");
    }

    @Test
    void findPendingJobs_shouldOrderByPriorityDesc() {
        Job lowPriority = Job.builder()
                .type("DATA_CLEANUP")
                .payload("hello")
                .status(JobStatus.PENDING)
                .attempts(0)
                .maxAttempts(3)
                .priority(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Job highPriority = Job.builder()
                .type("EMAIL_NOTIFICATION")
                .payload("hello")
                .status(JobStatus.PENDING)
                .attempts(0)
                .maxAttempts(3)
                .priority(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        jobRepository.save(lowPriority);
        jobRepository.save(highPriority);

        List<Job> result = jobRepository.findPendingJobs(JobStatus.PENDING, LocalDateTime.now());

        assertThat(result.get(0).getPriority()).isEqualTo(10);
        assertThat(result.get(1).getPriority()).isEqualTo(1);
    }
}