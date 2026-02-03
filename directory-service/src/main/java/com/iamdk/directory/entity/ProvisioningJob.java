package com.iamdk.directory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Provisioning Job Entity
 * Tracks user provisioning job executions
 */
@Entity
@Table(name = "provisioning_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvisioningJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String jobName;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Column(length = 500)
    private String sourceLocation; // File path or REST URL

    @Column(nullable = false)
    private Boolean dryRun;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(nullable = false)
    private Integer totalProcessed;

    @Builder.Default
    private Integer createdCount = 0;

    @Builder.Default
    private Integer updatedCount = 0;

    @Builder.Default
    private Integer deactivatedCount = 0;

    @Builder.Default
    private Integer failedCount = 0;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(length = 100)
    private String triggeredBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum SourceType {
        CSV,
        REST
    }

    public enum JobStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED
    }

    public void start() {
        this.status = JobStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = JobStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }
}
