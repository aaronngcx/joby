package com.example.joby.controller;

import com.example.joby.model.Job;
import com.example.joby.model.JobStatus;
import com.example.joby.service.JobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "joby.api-key=test-key",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.kafka.bootstrap-servers=localhost:9092"
})
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobService jobService;

    @Test
    void enqueue_shouldReturn200WithJob() throws Exception {
        Job savedJob = Job.builder()
                .id(1L)
                .type("EMAIL_NOTIFICATION")
                .payload("hello")
                .status(JobStatus.PENDING)
                .priority(0)
                .attempts(0)
                .maxAttempts(3)
                .build();

        when(jobService.enqueue(anyString(), anyString(), anyInt())).thenReturn(savedJob);

        Map<String, Object> request = Map.of(
                "type", "EMAIL_NOTIFICATION",
                "payload", "hello",
                "priority", 0
        );

        mockMvc.perform(post("/api/jobs")
                .header("X-API-KEY", "test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("EMAIL_NOTIFICATION"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void enqueue_shouldReturn400WhenTypeIsMissing() throws Exception {
        Map<String, Object> request = Map.of(
                "payload", "hello",
                "priority", 0
        );

        mockMvc.perform(post("/api/jobs")
                .header("X-API-KEY", "test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.type").value("type is required"));
    }

    @Test
    void getAllJobs_shouldReturnListOfJobs() throws Exception {
        Job job1 = Job.builder().id(1L).type("EMAIL_NOTIFICATION").status(JobStatus.PENDING).build();
        Job job2 = Job.builder().id(2L).type("DATA_CLEANUP").status(JobStatus.DONE).build();

        when(jobService.getAllJobs()).thenReturn(List.of(job1, job2));

        mockMvc.perform(get("/api/jobs")
                .header("X-API-KEY", "test-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("EMAIL_NOTIFICATION"))
                .andExpect(jsonPath("$[1].type").value("DATA_CLEANUP"));
    }

    @Test
    void getByStatus_shouldReturnFilteredJobs() throws Exception {
        Job job = Job.builder().id(1L).type("EMAIL_NOTIFICATION").status(JobStatus.PENDING).build();

        when(jobService.getJobsByStatus(JobStatus.PENDING)).thenReturn(List.of(job));

        mockMvc.perform(get("/api/jobs/status/PENDING")
                .header("X-API-KEY", "test-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }
}