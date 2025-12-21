package com.jobprocessor.job_processor_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobProcessorBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobProcessorBackendApplication.class, args);
	}

}
