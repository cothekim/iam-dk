package com.iamdk.directory.service;

import com.iamdk.directory.dto.scim.ScimGroup;
import com.iamdk.directory.entity.Group;
import com.iamdk.directory.entity.User;
import com.iamdk.directory.repository.GroupRepository;
import com.iamdk.directory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Group Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    /**
     * Create a new group
     */
    @Transactional
    public Group createGroup(Group group) {
        if (groupRepository.findByName(group.getName()).isPresent()) {
            throw new IllegalArgumentException("Group with name '" + group.getName() + "' already exists");
        }

        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());

        return groupRepository.save(group);
    }

    /**
     * Update an existing group
     */
    @Transactional
    public Group updateGroup(Long id, Group group) {
        Group existing = getGroupById(id);

        if (!existing.getName().equals(group.getName()) &&
            groupRepository.findByName(group.getName()).isPresent()) {
            throw new IllegalArgumentException("Group with name '" + group.getName() + "' already exists");
        }

        existing.setName(group.getName());
        existing.setDescription(group.getDescription());
        existing.setUpdatedAt(LocalDateTime.now());

        return groupRepository.save(existing);
    }

    /**
     * Get group by ID
     */
    public Group getGroupById(Long id) {
        return groupRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + id));
    }

    /**
     * Get group by name
     */
    public Optional<Group> getGroupByName(String name) {
        return groupRepository.findByName(name);
    }

    /**
     * Search groups
     */
    public Page<Group> searchGroups(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return groupRepository.findAll(pageable);
        }
        return groupRepository.findByNameContainingIgnoreCase(query, pageable);
    }

    /**
     * Delete group
     */
    @Transactional
    public void deleteGroup(Long id) {
        Group group = getGroupById(id);
        groupRepository.delete(group);
    }

    /**
     * Add users to a group
     */
    @Transactional
    public Group addUsersToGroup(Long groupId, Set<Long> userIds) {
        Group group = getGroupById(groupId);

        List<User> users = userRepository.findAllById(userIds);
        for (User user : users) {
            user.getGroups().add(group);
            userRepository.save(user);
        }

        return groupRepository.findById(groupId).orElse(group);
    }

    /**
     * Remove users from a group
     */
    @Transactional
    public Group removeUsersFromGroup(Long groupId, Set<Long> userIds) {
        Group group = getGroupById(groupId);

        List<User> users = userRepository.findAllById(userIds);
        for (User user : users) {
            user.getGroups().remove(group);
            userRepository.save(user);
        }

        return groupRepository.findById(groupId).orElse(group);
    }

    /**
     * Set group members (replace all)
     */
    @Transactional
    public Group setGroupMembers(Long groupId, Set<Long> userIds) {
        Group group = getGroupById(groupId);

        // Remove all existing users
        for (User user : group.getUsers()) {
            user.getGroups().remove(group);
            userRepository.save(user);
        }

        // Add new users
        List<User> users = userRepository.findAllById(userIds);
        for (User user : users) {
            user.getGroups().add(group);
            userRepository.save(user);
        }

        return groupRepository.findById(groupId).orElse(group);
    }

    /**
     * Convert Group to ScimGroup
     */
    public ScimGroup toScimGroup(Group group, String baseUrl) {
        List<ScimGroup.Member> members = group.getUsers().stream()
            .map(user -> ScimGroup.Member.builder()
                .value(user.getId().toString())
                .display(user.getLoginName())
                .ref(baseUrl + "/api/scim/v2/users/" + user.getId())
                .type("User")
                .build())
            .collect(Collectors.toList());

        return ScimGroup.builder()
            .id(group.getId().toString())
            .displayName(group.getName())
            .members(members)
            .meta(ScimGroup.Meta.builder()
                .resourceType("Group")
                .created(group.getCreatedAt().toString())
                .lastModified(group.getUpdatedAt().toString())
                .location(baseUrl + "/api/scim/v2/groups/" + group.getId())
                .build())
            .build();
    }
}
