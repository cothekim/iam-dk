package com.iamdk.directory.repository;

import com.iamdk.directory.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginName(String loginName);

    Optional<User> findByEmail(String email);

    List<User> findByLoginNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String loginName, String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.loginName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> findByLoginNameOrEmailContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.groups g WHERE g.name = :groupName")
    List<User> findByGroupName(@Param("groupName") String groupName);

    List<User> findByActive(Boolean active);
}
