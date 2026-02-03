package com.iamdk.directory.service;

import com.iamdk.directory.dto.scim.ScimUser;
import com.iamdk.directory.entity.User;
import com.iamdk.directory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new user
     */
    @Transactional
    public User createUser(User user) {
        if (userRepository.findByLoginName(user.getLoginName()).isPresent()) {
            throw new IllegalArgumentException("User with loginName '" + user.getLoginName() + "' already exists");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email '" + user.getEmail() + "' already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Update an existing user
     */
    @Transactional
    public User updateUser(Long id, User user) {
        User existing = getUserById(id);

        if (!existing.getLoginName().equals(user.getLoginName()) &&
            userRepository.findByLoginName(user.getLoginName()).isPresent()) {
            throw new IllegalArgumentException("User with loginName '" + user.getLoginName() + "' already exists");
        }

        if (!existing.getEmail().equals(user.getEmail()) &&
            userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email '" + user.getEmail() + "' already exists");
        }

        existing.setLoginName(user.getLoginName());
        existing.setEmail(user.getEmail());
        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        existing.setPhone(user.getPhone());
        existing.setDepartment(user.getDepartment());
        existing.setTitle(user.getTitle());
        existing.setActive(user.getActive());
        existing.setAttributes(user.getAttributes());

        return userRepository.save(existing);
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    /**
     * Get user by login name
     */
    public Optional<User> getUserByLoginName(String loginName) {
        return userRepository.findByLoginName(loginName);
    }

    /**
     * Search users
     */
    public Page<User> searchUsers(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findByLoginNameOrEmailContainingIgnoreCase(query, pageable);
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Record failed login attempt
     */
    @Transactional
    public void recordFailedLogin(String loginName, int maxAttempts, int lockoutMinutes) {
        Optional<User> userOpt = userRepository.findByLoginName(loginName);
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        if (user.getFailedLoginAttempts() >= maxAttempts) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
            log.warn("User {} locked due to too many failed attempts", loginName);
        }

        userRepository.save(user);
    }

    /**
     * Record successful login
     */
    @Transactional
    public void recordSuccessfulLogin(String loginName) {
        Optional<User> userOpt = userRepository.findByLoginName(loginName);
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Upsert user from provisioning source
     */
    @Transactional
    public User upsertUser(String loginName, String email, String firstName, String lastName, Boolean active) {
        Optional<User> userOpt = userRepository.findByLoginName(loginName);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setActive(active != null ? active : true);
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        } else {
            User newUser = User.builder()
                .loginName(loginName)
                .email(email)
                .password(passwordEncoder.encode("ChangeMe123!"))
                .firstName(firstName)
                .lastName(lastName)
                .active(active != null ? active : true)
                .failedLoginAttempts(0)
                .build();
            return userRepository.save(newUser);
        }
    }

    /**
     * Convert User to ScimUser
     */
    public ScimUser toScimUser(User user, String baseUrl) {
        return ScimUser.builder()
            .id(user.getId().toString())
            .userName(user.getLoginName())
            .name(ScimUser.Name.builder()
                .givenName(user.getFirstName())
                .familyName(user.getLastName())
                .build())
            .displayName(user.getFirstName() + " " + user.getLastName())
            .emails(List.of(ScimUser.Email.builder()
                .value(user.getEmail())
                .type("work")
                .primary(true)
                .build()))
            .active(user.getActive())
            .enterprise(ScimUser.EnterpriseExtension.builder()
                .department(user.getDepartment())
                .title(user.getTitle())
                .build())
            .meta(ScimUser.Meta.builder()
                .resourceType("User")
                .created(user.getCreatedAt().toString())
                .lastModified(user.getUpdatedAt().toString())
                .location(baseUrl + "/api/scim/v2/users/" + user.getId())
                .build())
            .build();
    }
}
