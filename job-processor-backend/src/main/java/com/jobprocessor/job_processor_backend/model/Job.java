package com.jobprocessor.job_processor_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "jobs", indexes = {
        // Index for the dashboard and worker performance
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_tenant_id", columnList = "tenantId")
})
@Data
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    //Idempotency key to avoid duplicate jobs
    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    //Tenant ID for rate limiting
    @Column(nullable = false)
    private String tenantId;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.PENDING;

    private int retryCount = 0;

    // RLease tracking
    private LocalDateTime lockedAt;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}