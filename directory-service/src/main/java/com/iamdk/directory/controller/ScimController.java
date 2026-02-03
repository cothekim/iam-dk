package com.iamdk.directory.controller;

import com.iamdk.directory.dto.scim.ScimErrorResponse;
import com.iamdk.directory.dto.scim.ScimGroup;
import com.iamdk.directory.dto.scim.ScimUser;
import com.iamdk.directory.entity.Group;
import com.iamdk.directory.entity.User;
import com.iamdk.directory.service.GroupService;
import com.iamdk.directory.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SCIM 2.0 API Controller
 * Provides SCIM-lite endpoints for user and group management
 * https://datatracker.ietf.org/doc/html/rfc7643
 */
@Slf4j
@RestController
@RequestMapping("/api/scim/v2")
@RequiredArgsConstructor
public class ScimController {

    private final UserService userService;
    private final GroupService groupService;

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        return scheme + "://" + serverName + ":" + serverPort + contextPath;
    }

    // ==================== Users ====================

    /**
     * Get users with optional filter
     * GET /Users?filter=userName eq "john.doe"
     * GET /Users?startIndex=1&count=100
     */
    @GetMapping("/Users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Integer startIndex,
            @RequestParam(required = false) Integer count,
            HttpServletRequest request) {

        String baseUrl = getBaseUrl(request);
        int page = (startIndex != null && startIndex > 0) ? (startIndex - 1) : 0;
        int size = (count != null && count > 0) ? count : 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by("loginName"));

        String search = null;
        if (filter != null) {
            // Parse simple filter: userName eq "value" or emails.value eq "value"
            if (filter.contains("userName eq")) {
                String value = filter.split("eq")[1].trim().replaceAll("[\"']", "");
                search = value;
            } else if (filter.contains("emails.value eq")) {
                String value = filter.split("eq")[1].trim().replaceAll("[\"']", "");
                search = value;
            }
        }

        Page<User> users = userService.searchUsers(search, pageable);

        ScimUserListResponse response = ScimUserListResponse.builder()
            .schemas(List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse"))
            .totalResults((int) users.getTotalElements())
            .startIndex(page * size + 1)
            .itemsPerPage(users.getNumberOfElements())
            .Resources(users.getContent().stream()
                .map(user -> userService.toScimUser(user, baseUrl))
                .collect(Collectors.toList()))
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get user by ID
     * GET /Users/{id}
     */
    @GetMapping("/Users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScimUser> getUser(@PathVariable Long id, HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userService.toScimUser(user, baseUrl));
    }

    /**
     * Create user
     * POST /Users
     */
    @PostMapping("/Users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScimUser> createUser(@RequestBody ScimUser scimUser, HttpServletRequest request) {
        User user = User.builder()
            .loginName(scimUser.getUserName())
            .email(scimUser.getEmails() != null && !scimUser.getEmails().isEmpty() ?
                scimUser.getEmails().get(0).getValue() : null)
            .password("ChangeMe123!")
            .firstName(scimUser.getName() != null ? scimUser.getName().getGivenName() : "")
            .lastName(scimUser.getName() != null ? scimUser.getName().getFamilyName() : "")
            .active(scimUser.getActive() != null ? scimUser.getActive() : true)
            .build();

        User created = userService.createUser(user);
        String baseUrl = getBaseUrl(request);
        ScimUser response = userService.toScimUser(created, baseUrl);

        return ResponseEntity
            .created(URI.create(response.getMeta().getLocation()))
            .body(response);
    }

    /**
     * Update user (PUT - full replace)
     * PUT /Users/{id}
     */
    @PutMapping("/Users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScimUser> updateUser(@PathVariable Long id, @RequestBody ScimUser scimUser, HttpServletRequest request) {
        User user = User.builder()
            .loginName(scimUser.getUserName())
            .email(scimUser.getEmails() != null && !scimUser.getEmails().isEmpty() ?
                scimUser.getEmails().get(0).getValue() : null)
            .firstName(scimUser.getName() != null ? scimUser.getName().getGivenName() : "")
            .lastName(scimUser.getName() != null ? scimUser.getName().getFamilyName() : "")
            .active(scimUser.getActive())
            .build();

        User updated = userService.updateUser(id, user);
        String baseUrl = getBaseUrl(request);
        return ResponseEntity.ok(userService.toScimUser(updated, baseUrl));
    }

    /**
     * Delete user
     * DELETE /Users/{id}
     */
    @DeleteMapping("/Users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Groups ====================

    /**
     * Get groups
     * GET /Groups
     */
    @GetMapping("/Groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getGroups(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Integer startIndex,
            @RequestParam(required = false) Integer count,
            HttpServletRequest request) {

        String baseUrl = getBaseUrl(request);
        int page = (startIndex != null && startIndex > 0) ? (startIndex - 1) : 0;
        int size = (count != null && count > 0) ? count : 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));

        String search = null;
        if (filter != null && filter.contains("displayName eq")) {
            String value = filter.split("eq")[1].trim().replaceAll("[\"']", "");
            search = value;
        }

        Page<Group> groups = groupService.searchGroups(search, pageable);

        ScimGroupListResponse response = ScimGroupListResponse.builder()
            .schemas(List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse"))
            .totalResults((int) groups.getTotalElements())
            .startIndex(page * size + 1)
            .itemsPerPage(groups.getNumberOfElements())
            .Resources(groups.getContent().stream()
                .map(group -> groupService.toScimGroup(group, baseUrl))
                .collect(Collectors.toList()))
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get group by ID
     * GET /Groups/{id}
     */
    @GetMapping("/Groups/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScimGroup> getGroup(@PathVariable Long id, HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        Group group = groupService.getGroupById(id);
        return ResponseEntity.ok(groupService.toScimGroup(group, baseUrl));
    }

    /**
     * Create group
     * POST /Groups
     */
    @PostMapping("/Groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScimGroup> createGroup(@RequestBody ScimGroup scimGroup, HttpServletRequest request) {
        Group group = Group.builder()
            .name(scimGroup.getDisplayName())
            .description("")
            .build();

        Group created = groupService.createGroup(group);
        String baseUrl = getBaseUrl(request);
        ScimGroup response = groupService.toScimGroup(created, baseUrl);

        return ResponseEntity
            .created(URI.create(response.getMeta().getLocation()))
            .body(response);
    }

    /**
     * Update group
     * PUT /Groups/{id}
     */
    @PutMapping("/Groups/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScimGroup> updateGroup(@PathVariable Long id, @RequestBody ScimGroup scimGroup, HttpServletRequest request) {
        Group group = Group.builder()
            .name(scimGroup.getDisplayName())
            .build();

        Group updated = groupService.updateGroup(id, group);
        String baseUrl = getBaseUrl(request);
        return ResponseEntity.ok(groupService.toScimGroup(updated, baseUrl));
    }

    /**
     * Delete group
     * DELETE /Groups/{id}
     */
    @DeleteMapping("/Groups/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Error Handlers ====================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ScimErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ScimErrorResponse error = ScimErrorResponse.builder()
            .status(String.valueOf(HttpStatus.BAD_REQUEST.value()))
            .scimType("invalidSyntax")
            .detail(e.getMessage())
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ==================== DTOs ====================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ScimUserListResponse {
        private List<String> schemas;
        private Integer totalResults;
        private Integer startIndex;
        private Integer itemsPerPage;
        private List<ScimUser> Resources;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ScimGroupListResponse {
        private List<String> schemas;
        private Integer totalResults;
        private Integer startIndex;
        private Integer itemsPerPage;
        private List<ScimGroup> Resources;
    }
}
