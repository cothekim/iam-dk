package com.iamdk.directory.repository;

import com.iamdk.directory.entity.ProvisioningJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Provisioning Job Repository
 */
@Repository
public interface ProvisioningJobRepository extends JpaRepository<ProvisioningJob, Long> {

    List<ProvisioningJob> findByOrderByCreatedAtDesc();
}
