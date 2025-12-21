package com.jobprocessor.job_processor_backend.repository;

import com.jobprocessor.job_processor_backend.model.Job;
import com.jobprocessor.job_processor_backend.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    Optional<Job> findByIdempotencyKey(String idempotencyKey);

    /**
     * The "Lease" mechanism with Tenant Quotas
     * This query finds one PENDING job and locks it so other workers skip it
     * It ensures a tenant doesn't exceed 5 concurrent running jobs
     */
    @Query(value = """
        SELECT * FROM jobs 
        WHERE status = 'PENDING' 
        AND tenant_id NOT IN (
            SELECT tenant_id FROM (
                SELECT tenant_id FROM jobs 
                WHERE status = 'RUNNING' 
                GROUP BY tenant_id 
                HAVING COUNT(*) >= 5
            ) AS tenant_limits
        )
        ORDER BY created_at ASC 
        LIMIT 1 
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    Optional<Job> findNextJobToProcess();


    long countByStatus(JobStatus status);
}