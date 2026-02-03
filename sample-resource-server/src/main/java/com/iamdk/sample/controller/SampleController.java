package com.iamdk.sample.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample Resource Server Controller
 * Demonstrates authenticated endpoint access
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class SampleController {

    /**
     * Public endpoint - no authentication required
     */
    @GetMapping("/public/hello")
    public ResponseEntity<Map<String, Object>> publicHello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello, World! This is a public endpoint.");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    /**
     * Protected endpoint - requires valid JWT token
     */
    @GetMapping("/protected/hello")
    public ResponseEntity<Map<String, Object>> protectedHello(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello! You are authenticated.");
        response.put("timestamp", LocalDateTime.now());
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(response);
    }

    /**
     * Admin endpoint - requires valid JWT token
     */
    @GetMapping("/protected/admin")
    public ResponseEntity<Map<String, Object>> adminEndpoint(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello Admin! This is an admin endpoint.");
        response.put("timestamp", LocalDateTime.now());

        // Extract claims from JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", jwt.getSubject());
        claims.put("scope", jwt.getClaimAsString("scope"));
        claims.put("iss", jwt.getIssuer());
        claims.put("exp", jwt.getExpiresAt());
        response.put("jwtClaims", claims);

        return ResponseEntity.ok(response);
    }

    /**
     * User profile endpoint
     */
    @GetMapping("/protected/profile")
    public ResponseEntity<Map<String, Object>> profile(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("sub", jwt.getSubject());
        profile.put("iss", jwt.getIssuer().toString());
        profile.put("exp", jwt.getExpiresAt().toString());
        profile.put("iat", jwt.getIssuedAt().toString());
        profile.put("scope", jwt.getClaimAsString("scope"));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Your profile information");
        response.put("profile", profile);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
