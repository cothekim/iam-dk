package com.iamdk.directory.repository;

import com.iamdk.directory.entity.OAuthClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OAuth Client Repository
 */
@Repository
public interface OAuthClientRepository extends JpaRepository<OAuthClient, Long> {

    Optional<OAuthClient> findByClientId(String clientId);

    List<OAuthClient> findByEnabled(Boolean enabled);
}
