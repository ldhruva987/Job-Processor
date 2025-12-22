package com.jobprocessor.job_processor_backend.service;

import com.jobprocessor.job_processor_backend.model.Job;
import com.jobprocessor.job_processor_backend.model.JobStatus;
import com.jobprocessor.job_processor_backend.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // Rolls back changes after each test so your DB stays clean
public class JobServiceTest {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobRepository jobRepository;

    @Test
    void testCreateJobAndRetryLogic() {
        // 1. Test Creation
        Job job = new Job();
        job.setTenantId("test-tenant");
        job.setPayload("{}");
        job.setIdempotencyKey("unique-key-" + System.currentTimeMillis());

        Job savedJob = jobService.createJob(job);

        assertNotNull(savedJob.getId());
        assertEquals(JobStatus.PENDING, savedJob.getStatus());

        // 2. Test Failure/Retry Logic (Requirement 3.3)
        jobService.handleJobFailure(savedJob, "First failure");

        Job retriedJob = jobRepository.findById(savedJob.getId()).get();
        assertEquals(1, retriedJob.getRetryCount());
        assertEquals(JobStatus.PENDING, retriedJob.getStatus());

        // 3. Test DLQ transition (Requirement 3.4)
        // Force retry count to max
        retriedJob.setRetryCount(3);
        jobService.handleJobFailure(retriedJob, "Final failure");

        Job dlqJob = jobRepository.findById(savedJob.getId()).get();
        assertEquals(JobStatus.DLQ, dlqJob.getStatus());
    }
}