package com.example.joby.service;

import com.example.joby.model.Job;
import com.example.joby.model.JobStatus;
import com.example.joby.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    @Test
    void enqueue_shouldCreateJobWithPendingStatus() {
        // arrange
        Job savedJob = Job.builder()
                .id(1L)
                .type("EMAIL_NOTIFICATION")
                .payload("hello")
                .status(JobStatus.PENDING)
                .priority(0)
                .attempts(0)
                .maxAttempts(3)
                .build();

        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

        // act
        Job result = jobService.enqueue("EMAIL_NOTIFICATION", "hello", 0);

        // assert
        assertThat(result.getStatus()).isEqualTo(JobStatus.PENDING);
        assertThat(result.getType()).isEqualTo("EMAIL_NOTIFICATION");
        assertThat(result.getAttempts()).isEqualTo(0);
        assertThat(result.getMaxAttempts()).isEqualTo(3);
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    void getJobsByStatus_shouldReturnFilteredJobs() {
        // arrange
        Job job1 = Job.builder().id(1L).status(JobStatus.PENDING).build();
        Job job2 = Job.builder().id(2L).status(JobStatus.PENDING).build();
        when(jobRepository.findByStatus(JobStatus.PENDING)).thenReturn(List.of(job1, job2));

        // act
        List<Job> result = jobService.getJobsByStatus(JobStatus.PENDING);

        // assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(j -> j.getStatus() == JobStatus.PENDING);
        verify(jobRepository, times(1)).findByStatus(JobStatus.PENDING);
    }

    @Test
    void enqueue_shouldSaveToRepository() {
        // arrange
        when(jobRepository.save(any(Job.class))).thenAnswer(i -> i.getArgument(0));

        // act
        Job result = jobService.enqueue("REPORT_GENERATION", "payload", 5);

        // assert
        assertThat(result.getType()).isEqualTo("REPORT_GENERATION");
        assertThat(result.getPriority()).isEqualTo(5);
        assertThat(result.getStatus()).isEqualTo(JobStatus.PENDING);
    }
}