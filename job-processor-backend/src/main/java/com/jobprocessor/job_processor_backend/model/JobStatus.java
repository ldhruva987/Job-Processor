package com.jobprocessor.job_processor_backend.model;

public enum JobStatus {
    PENDING,
    RUNNING,
    DONE,
    FAILED,
    DLQ
}