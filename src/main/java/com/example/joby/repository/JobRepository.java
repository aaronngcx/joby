package com.example.joby.repository;

import com.example.joby.model.Job;
import com.example.joby.model.JobStatus;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(JobStatus status);

    @Query("SELECT j FROM Job j WHERE j.status = :status AND (j.scheduledAt IS NULL OR j.scheduledAt <= :now) ORDER BY j.priority DESC, j.createdAt ASC")
    List<Job> findPendingJobs(@Param("status") JobStatus status, @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.status = 'RUNNING', j.updatedAt = :now WHERE j.id = :id AND j.status = 'PENDING'")
    int claimJob(@Param("id") Long id, @Param("now") LocalDateTime now);


}

