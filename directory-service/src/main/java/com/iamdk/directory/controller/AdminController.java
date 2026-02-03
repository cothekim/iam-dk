package com.iamdk.directory.controller;

import com.iamdk.directory.entity.OAuthClient;
import com.iamdk.directory.entity.User;
import com.iamdk.directory.security.JwtService;
import com.iamdk.directory.service.OAuthClientService;
import com.iamdk.directory.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Admin API Controller
 * Provides endpoints for admin console UI
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final OAuthClientService clientService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Admin login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            String token = jwtService.generateToken(request.username());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
        }
    }

    // ==================== User Management ====================

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<User>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userService.searchUsers(search, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User user = User.builder()
            .loginName(request.loginName())
            .email(request.email())
            .password(request.password())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .phone(request.phone())
            .department(request.department())
            .title(request.title())
            .active(request.active() != null ? request.active() : true)
            .attributes(request.attributes())
            .build();

        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        User user = User.builder()
            .loginName(request.loginName())
            .email(request.email())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .phone(request.phone())
            .department(request.department())
            .title(request.title())
            .active(request.active())
            .attributes(request.attributes())
            .build();

        User updated = userService.updateUser(id, user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request.password());
        return ResponseEntity.ok().build();
    }

    // ==================== OAuth Client Management ====================

    @GetMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OAuthClient>> getClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OAuthClient> clients = clientService.searchClients(pageable);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/clients/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OAuthClient> getClient(@PathVariable Long id) {
        OAuthClient client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @PostMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OAuthClient> createClient(@RequestBody OAuthClient client) {
        OAuthClient created = clientService.createClient(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/clients/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OAuthClient> updateClient(@PathVariable Long id, @RequestBody OAuthClient client) {
        OAuthClient updated = clientService.updateClient(id, client);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/clients/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clients/{id}/regenerate-secret")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OAuthClient> regenerateSecret(@PathVariable Long id) {
        String newSecret = generateRandomSecret();
        OAuthClient updated = clientService.regenerateSecret(id, newSecret);
        return ResponseEntity.ok(updated);
    }

    private String generateRandomSecret() {
        // Generate a random 32-character secret
        return java.util.UUID.randomUUID().toString().replace("-", "") +
               java.util.UUID.randomUUID().toString().replace("-", "");
    }

    // ==================== DTOs ====================

    public record LoginRequest(String username, String password) {}

    public record CreateUserRequest(
        String loginName,
        String email,
        String password,
        String firstName,
        String lastName,
        String phone,
        String department,
        String title,
        Boolean active,
        java.util.Map<String, Object> attributes
    ) {}

    public record UpdateUserRequest(
        String loginName,
        String email,
        String firstName,
        String lastName,
        String phone,
        String department,
        String title,
        Boolean active,
        java.util.Map<String, Object> attributes
    ) {}

    public record ChangePasswordRequest(String password) {}
}
