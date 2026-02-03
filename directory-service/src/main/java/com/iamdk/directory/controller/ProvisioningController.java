package com.iamdk.directory.controller;

import com.iamdk.directory.entity.ProvisioningJob;
import com.iamdk.directory.service.ProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Provisioning Controller
 * Handles CSV upload and job management
 */
@Slf4j
@RestController
@RequestMapping("/api/provisioning")
@RequiredArgsConstructor
public class ProvisioningController {

    private final ProvisioningService provisioningService;

    /**
     * Create a new provisioning job
     */
    @PostMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProvisioningJob> createJob(@RequestBody CreateJobRequest request) {
        ProvisioningJob job = provisioningService.createJob(
            request.jobName(),
            request.sourceLocation(),
            request.triggeredBy()
        );
        return ResponseEntity.status(201).body(job);
    }

    /**
     * Execute a provisioning job with CSV file
     */
    @PostMapping("/jobs/{id}/execute")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProvisioningJob> executeJob(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun) {

        ProvisioningJob job = provisioningService.executeJob(id, file, dryRun);
        return ResponseEntity.ok(job);
    }

    /**
     * Get job history
     */
    @GetMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProvisioningJob>> getJobHistory() {
        List<ProvisioningJob> jobs = provisioningService.getJobHistory();
        return ResponseEntity.ok(jobs);
    }

    /**
     * Get job by ID
     */
    @GetMapping("/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProvisioningJob> getJob(@PathVariable Long id) {
        ProvisioningJob job = provisioningService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    /**
     * Get CSV template info
     */
    @GetMapping("/template")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTemplateInfo() {
        return ResponseEntity.ok(Map.of(
            "columns", List.of("loginName", "email", "firstName", "lastName", "active"),
            "requiredFields", List.of("loginName", "email", "firstName", "lastName"),
            "example", "loginName,email,firstName,lastName,active\njohn.doe,john@example.com,John,Doe,true",
            "maxRows", 5000
        ));
    }

    // ==================== DTOs ====================

    public record CreateJobRequest(
        String jobName,
        String sourceLocation,
        String triggeredBy
    ) {}
}
