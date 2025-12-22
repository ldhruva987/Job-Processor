package com.jobprocessor.job_processor_backend.controller;

import com.jobprocessor.job_processor_backend.model.Job;
import com.jobprocessor.job_processor_backend.model.JobStatus;
import com.jobprocessor.job_processor_backend.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    @PostMapping
    public ResponseEntity<Job> submitJob(@RequestBody Job jobRequest) {
        //Support optional idempotency key
        if (jobRequest.getIdempotencyKey() != null) {
            Optional<Job> existingJob = jobRepository.findByIdempotencyKey(jobRequest.getIdempotencyKey());
            if (existingJob.isPresent()) {
                // Return existing job instead of creating a duplicate
                return ResponseEntity.ok(existingJob.get());
            }
        }

        // Save the new job to persistence
        Job savedJob = jobRepository.save(jobRequest);
        return new ResponseEntity<>(savedJob, HttpStatus.CREATED);
    }
    // Inside JobController.java

    @GetMapping("/recent")
    public ResponseEntity<List<Job>> getRecentJobs() {

        return ResponseEntity.ok(jobRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, 10,
                        org.springframework.data.domain.Sort.by("updatedAt").descending())
        ).getContent());
    }
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", jobRepository.count());
        stats.put("pending", jobRepository.countByStatus(JobStatus.PENDING));
        stats.put("running", jobRepository.countByStatus(JobStatus.RUNNING));
        stats.put("completed", jobRepository.countByStatus(JobStatus.DONE));
        stats.put("failed", jobRepository.countByStatus(JobStatus.FAILED));
        stats.put("dlq", jobRepository.countByStatus(JobStatus.DLQ)); // [cite: 29]

        return ResponseEntity.ok(stats);
    }

    // Checking job status
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobStatus(@PathVariable UUID id) {
        return jobRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}