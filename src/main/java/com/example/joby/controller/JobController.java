package com.example.joby.controller;

import com.example.joby.model.Job;
import com.example.joby.model.JobStatus;
import com.example.joby.service.JobService;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @Operation(summary = "Enqueue a new job")
    @PostMapping
    public ResponseEntity<Job> enqueue(@Valid @RequestBody EnqueueRequest request) {
        Job job = jobService.enqueue(request.type(), request.payload(), request.priority());
        return ResponseEntity.ok(job);
    }

    @Operation(summary = "Get all jobs")
    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @Operation(summary = "Get jobs by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Job>> getByStatus(@PathVariable JobStatus status) {
        return ResponseEntity.ok(jobService.getJobsByStatus(status));
    }

    @Operation(summary = "Get queue stats")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = Map.of(
                "pending", (long) jobService.getJobsByStatus(JobStatus.PENDING).size(),
                "running", (long) jobService.getJobsByStatus(JobStatus.RUNNING).size(),
                "done", (long) jobService.getJobsByStatus(JobStatus.DONE).size(),
                "failed", (long) jobService.getJobsByStatus(JobStatus.FAILED).size(),
                "dead", (long) jobService.getJobsByStatus(JobStatus.DEAD).size()
        );
        return ResponseEntity.ok(stats);
    }

    public record EnqueueRequest(
        @NotBlank(message = "type is required")
        String type,

        @NotBlank(message = "payload is required")
        String payload,

        int priority

    ) {}
}