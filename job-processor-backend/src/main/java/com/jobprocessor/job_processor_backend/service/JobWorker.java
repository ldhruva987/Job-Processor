package com.jobprocessor.job_processor_backend.service;

import com.jobprocessor.job_processor_backend.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JobWorker {

    private static final Logger log = LoggerFactory.getLogger(JobWorker.class);

    @Autowired
    private JobService jobService;

    // Workers poll jobs from the queue && Runs every 5 seconds to check for new work
    @Scheduled(fixedDelay = 5000)
    public void runWorker() {
        Optional<Job> leasedJob = jobService.leaseNextJob();

        leasedJob.ifPresent(this::processJob);
    }

    private void processJob(Job job) {
        MDC.put("jobId", job.getId().toString());

        try {
            log.info("Worker picked up job for tenant: {}", job.getTenantId());

            // In a real app, you'd parse job.getPayload() and execute logic here
            Thread.sleep(2000);
            jobService.acknowledgeJob(job);
            log.info("Job successfully completed");

        } catch (Exception e) {
            //Retry / DLQ logic
            log.error("Job processing failed: {}", e.getMessage());
            jobService.handleJobFailure(job, e.getMessage());
        } finally {
            MDC.clear();
        }
    }
}