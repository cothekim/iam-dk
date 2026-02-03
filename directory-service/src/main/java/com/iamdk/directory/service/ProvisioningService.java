package com.iamdk.directory.service;

import com.iamdk.directory.entity.ProvisioningJob;
import com.iamdk.directory.repository.ProvisioningJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Provisioning Service
 * Handles CSV-based user provisioning
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProvisioningService {

    private final ProvisioningJobRepository jobRepository;
    private final UserService userService;
    private final GroupService groupService;

    private static final Map<String, Function<CSVRecord, String>> DEFAULT_MAPPINGS = new HashMap<>();

    static {
        DEFAULT_MAPPINGS.put("loginName", record -> record.get("loginName"));
        DEFAULT_MAPPINGS.put("email", record -> record.get("email"));
        DEFAULT_MAPPINGS.put("firstName", record -> record.get("firstName"));
        DEFAULT_MAPPINGS.put("lastName", record -> record.get("lastName"));
        DEFAULT_MAPPINGS.put("active", record -> {
            String value = record.get("active").toLowerCase();
            return String.valueOf("true".equals(value) || "yes".equals(value) || "1".equals(value));
        });
    }

    /**
     * Create a provisioning job
     */
    @Transactional
    public ProvisioningJob createJob(String jobName, String sourceLocation, String triggeredBy) {
        ProvisioningJob job = ProvisioningJob.builder()
            .jobName(jobName)
            .sourceType(ProvisioningJob.SourceType.CSV)
            .sourceLocation(sourceLocation)
            .dryRun(false)
            .status(ProvisioningJob.JobStatus.PENDING)
            .totalProcessed(0)
            .createdCount(0)
            .updatedCount(0)
            .deactivatedCount(0)
            .failedCount(0)
            .triggeredBy(triggeredBy)
            .build();

        return jobRepository.save(job);
    }

    /**
     * Execute provisioning job from CSV file
     */
    @Transactional
    public ProvisioningJob executeJob(Long jobId, MultipartFile file, boolean dryRun) {
        ProvisioningJob job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + jobId));

        job.setDryRun(dryRun);

        try {
            job.start();
            jobRepository.save(job);

            // If not dry run, we'll commit changes
            if (!dryRun) {
                processCsvFile(job, file);
            } else {
                processCsvFileDryRun(job, file);
            }

            job.complete();
            log.info("Provisioning job {} completed: created={}, updated={}, deactivated={}, failed={}",
                jobId, job.getCreatedCount(), job.getUpdatedCount(),
                job.getDeactivatedCount(), job.getFailedCount());

        } catch (Exception e) {
            log.error("Provisioning job {} failed", jobId, e);
            job.fail(e.getMessage());
        }

        return jobRepository.save(job);
    }

    /**
     * Process CSV file with actual changes
     */
    private void processCsvFile(ProvisioningJob job, MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser parser = CSVFormat.DEFAULT
                 .withHeader()
                 .withIgnoreHeaderCase()
                 .withTrim()
                 .parse(reader)) {

            for (CSVRecord record : parser) {
                job.setTotalProcessed(job.getTotalProcessed() + 1);

                try {
                    String loginName = getRequiredField(record, "loginName");
                    String email = getRequiredField(record, "email");
                    String firstName = getRequiredField(record, "firstName");
                    String lastName = getRequiredField(record, "lastName");

                    String activeStr = record.get("active");
                    Boolean active = activeStr != null && !activeStr.isBlank() ?
                        parseBoolean(activeStr) : true;

                    var existing = userService.getUserByLoginName(loginName);
                    if (existing.isPresent()) {
                        userService.upsertUser(loginName, email, firstName, lastName, active);
                        job.setUpdatedCount(job.getUpdatedCount() + 1);
                    } else {
                        userService.upsertUser(loginName, email, firstName, lastName, active);
                        job.setCreatedCount(job.getCreatedCount() + 1);
                    }

                } catch (Exception e) {
                    log.warn("Failed to process record {}: {}", record.getRecordNumber(), e.getMessage());
                    job.setFailedCount(job.getFailedCount() + 1);
                }
            }
        }
    }

    /**
     * Process CSV file for dry run (no actual changes)
     */
    private void processCsvFileDryRun(ProvisioningJob job, MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser parser = CSVFormat.DEFAULT
                 .withHeader()
                 .withIgnoreHeaderCase()
                 .withTrim()
                 .parse(reader)) {

            for (CSVRecord record : parser) {
                job.setTotalProcessed(job.getTotalProcessed() + 1);

                try {
                    String loginName = getRequiredField(record, "loginName");
                    String email = getRequiredField(record, "email");
                    String firstName = getRequiredField(record, "firstName");
                    String lastName = getRequiredField(record, "lastName");

                    String activeStr = record.get("active");
                    Boolean active = activeStr != null && !activeStr.isBlank() ?
                        parseBoolean(activeStr) : true;

                    var existing = userService.getUserByLoginName(loginName);
                    if (existing.isPresent()) {
                        job.setUpdatedCount(job.getUpdatedCount() + 1);
                    } else {
                        job.setCreatedCount(job.getCreatedCount() + 1);
                    }

                } catch (Exception e) {
                    log.warn("Failed to process record {}: {}", record.getRecordNumber(), e.getMessage());
                    job.setFailedCount(job.getFailedCount() + 1);
                }
            }
        }
    }

    private String getRequiredField(CSVRecord record, String fieldName) {
        String value = record.get(fieldName);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + fieldName + "' is required");
        }
        return value;
    }

    private Boolean parseBoolean(String value) {
        String lower = value.toLowerCase();
        return "true".equals(lower) || "yes".equals(lower) || "1".equals(lower) || "y".equals(lower);
    }

    /**
     * Get job history
     */
    public java.util.List<ProvisioningJob> getJobHistory() {
        return jobRepository.findByOrderByCreatedAtDesc();
    }

    /**
     * Get job by ID
     */
    public ProvisioningJob getJobById(Long id) {
        return jobRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + id));
    }
}
