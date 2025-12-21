package com.jobprocessor.job_processor_backend.service;

import com.jobprocessor.job_processor_backend.model.Job;
import com.jobprocessor.job_processor_backend.model.JobStatus;
import com.jobprocessor.job_processor_backend.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    private static final int MAX_RETRIES = 3; // Requirement 3.3 [cite: 20, 21]

    //Logic for submitting a job with idempotency check
    @Transactional
    public Job createJob(Job job) {
        if (job.getIdempotencyKey() != null) {
            Optional<Job> existing = jobRepository.findByIdempotencyKey(job.getIdempotencyKey());
            if (existing.isPresent()) {
                return existing.get(); // Requirement 1.3 [cite: 11]
            }
        }
        job.setStatus(JobStatus.PENDING);
        return jobRepository.save(job); // Requirement 2 [cite: 13, 14]
    }

    //The Lease logic - Atomic fetch and lock
    @Transactional
    public Optional<Job> leaseNextJob() {
        return jobRepository.findNextJobToProcess(); // Uses SKIP LOCKED [cite: 17, 18]
    }

    //Ack logic - Mark as finished
    @Transactional
    public void acknowledgeJob(Job job) {
        job.setStatus(JobStatus.DONE);
        jobRepository.save(job); // [cite: 19]
    }

    //Retry and DLQ logic
    @Transactional
    public void handleJobFailure(Job job, String errorReason) {
        if (job.getRetryCount() < MAX_RETRIES) {
            job.setRetryCount(job.getRetryCount() + 1);
            job.setStatus(JobStatus.PENDING); // Re-queue [cite: 20]
        } else {
            job.setStatus(JobStatus.DLQ); // Dead Letter Queue
        }
        jobRepository.save(job);
    }
}