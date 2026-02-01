package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.User;
import com.backend.domain.enums.UserRole;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {

    // Authentication queries
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Role-based queries
    List<User> findByRole(UserRole role);

    List<User> findByRoleIn(List<UserRole> roles);

    // Status queries
    List<User> findByIsActive(Boolean isActive);

    List<User> findByEmailVerified(Boolean emailVerified);

    List<User> findByTwoFactorEnabled(Boolean twoFactorEnabled);

    long countByIsActive(Boolean isActive);

    // Account security queries
    List<User> findByFailedLoginAttemptsGreaterThanEqual(Integer attempts);

    List<User> findByLockedUntilAfter(LocalDateTime dateTime);

    List<User> findByPasswordChangedAtBefore(LocalDateTime dateTime);

    // Date range queries
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<User> findByLastLoginAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByLastLoginAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Activity queries
    List<User> findByLastLoginAtAfter(LocalDateTime dateTime);

    List<User> findByLastLoginAtBefore(LocalDateTime dateTime);

    List<User> findByLastLoginAtIsNull();

    long countByLastLoginAtAfter(LocalDateTime dateTime);

    // Search queries
    List<User> findByFullNameContainingIgnoreCase(String fullName);

    List<User> findByEmailContainingIgnoreCase(String email);

    // Department and job queries
    List<User> findByDepartment(String department);

    List<User> findByJobTitle(String jobTitle);

    // Role count
    long countByRole(UserRole role);

    // Bulk operations
    List<User> findByIdIn(List<Long> ids);
}