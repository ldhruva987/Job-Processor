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

    private static final int MAX_RETRIES = 3;

    //Logic for submitting a job with idempotency check
    @Transactional
    public Job createJob(Job job) {
        if (job.getIdempotencyKey() != null) {
            Optional<Job> existing = jobRepository.findByIdempotencyKey(job.getIdempotencyKey());
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        job.setStatus(JobStatus.PENDING);
        return jobRepository.save(job);
    }

    //The Lease logic - Atomic fetch and lock
    @Transactional
    public Optional<Job> leaseNextJob() {
        return jobRepository.findNextJobToProcess(); // Uses SKIP LOCKED
    }

    //Ack logic - Mark as finished
    @Transactional
    public void acknowledgeJob(Job job) {
        job.setStatus(JobStatus.DONE);
        jobRepository.save(job);
    }

    //Retry and DLQ logic
    @Transactional
    public void handleJobFailure(Job job, String errorReason) {
        if (job.getRetryCount() < MAX_RETRIES) {
            job.setRetryCount(job.getRetryCount() + 1);
            job.setStatus(JobStatus.PENDING);
        } else {
            job.setStatus(JobStatus.DLQ); // Dead Letter Queue
        }
        jobRepository.save(job);
    }
}