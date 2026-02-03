package com.iamdk.directory.service;

import com.iamdk.directory.entity.OAuthClient;
import com.iamdk.directory.repository.OAuthClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * OAuth Client Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthClientService {

    private final OAuthClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new OAuth client
     */
    @Transactional
    public OAuthClient createClient(OAuthClient client) {
        if (clientRepository.findByClientId(client.getClientId()).isPresent()) {
            throw new IllegalArgumentException("OAuth client with clientId '" + client.getClientId() + "' already exists");
        }

        client.setClientSecret(passwordEncoder.encode(client.getClientSecret()));
        client.setEnabled(true);
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());

        return clientRepository.save(client);
    }

    /**
     * Update an existing OAuth client
     */
    @Transactional
    public OAuthClient updateClient(Long id, OAuthClient client) {
        OAuthClient existing = getClientById(id);

        if (!existing.getClientId().equals(client.getClientId()) &&
            clientRepository.findByClientId(client.getClientId()).isPresent()) {
            throw new IllegalArgumentException("OAuth client with clientId '" + client.getClientId() + "' already exists");
        }

        existing.setClientId(client.getClientId());
        existing.setName(client.getName());
        existing.setDescription(client.getDescription());
        existing.setRedirectUris(client.getRedirectUris());
        existing.setGrantTypes(client.getGrantTypes());
        existing.setScopes(client.getScopes());

        // Only update secret if provided
        if (client.getClientSecret() != null && !client.getClientSecret().isBlank()) {
            existing.setClientSecret(passwordEncoder.encode(client.getClientSecret()));
        }

        existing.setEnabled(client.getEnabled());
        existing.setUpdatedAt(LocalDateTime.now());

        return clientRepository.save(existing);
    }

    /**
     * Get client by ID
     */
    public OAuthClient getClientById(Long id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("OAuth client not found with id: " + id));
    }

    /**
     * Get client by client ID
     */
    public Optional<OAuthClient> getClientByClientId(String clientId) {
        return clientRepository.findByClientId(clientId);
    }

    /**
     * Search clients
     */
    public Page<OAuthClient> searchClients(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    /**
     * Delete client
     */
    @Transactional
    public void deleteClient(Long id) {
        OAuthClient client = getClientById(id);
        clientRepository.delete(client);
    }

    /**
     * Regenerate client secret
     */
    @Transactional
    public OAuthClient regenerateSecret(Long id, String newSecret) {
        OAuthClient client = getClientById(id);
        client.setClientSecret(passwordEncoder.encode(newSecret));
        client.setUpdatedAt(LocalDateTime.now());
        return clientRepository.save(client);
    }

    /**
     * Validate client credentials
     */
    public boolean validateCredentials(String clientId, String clientSecret) {
        Optional<OAuthClient> clientOpt = clientRepository.findByClientId(clientId);
        if (clientOpt.isEmpty()) {
            return false;
        }

        OAuthClient client = clientOpt.get();
        return client.getEnabled() && passwordEncoder.matches(clientSecret, client.getClientSecret());
    }
}
