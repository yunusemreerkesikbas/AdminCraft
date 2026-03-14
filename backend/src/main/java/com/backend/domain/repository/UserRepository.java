package com.backend.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.domain.entity.User;
import com.backend.domain.enums.UserRole;

public interface UserRepository {

    // Basic CRUD operations
    User save(User user);

    List<User> saveAll(Iterable<User> users);

    Optional<User> findById(Long id);

    List<User> findAll();

    Page<User> findAll(Pageable pageable);

    void deleteById(Long id);

    boolean existsById(Long id);

    long count();

    Page<User> searchUsers(String search, Pageable pageable);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(UserRole role);

    List<User> findByRoleIn(List<UserRole> roles);

    long countByRole(UserRole role);

    // Status queries
    List<User> findByIsActive(Boolean isActive);

    long countByIsActive(Boolean isActive);

    List<User> findByEmailVerified(Boolean emailVerified);

    // Account security queries
    List<User> findByFailedLoginAttemptsGreaterThanEqual(Integer attempts);

    List<User> findByLockedUntilAfter(LocalDateTime dateTime);

    List<User> findByPasswordChangedAtBefore(LocalDateTime dateTime);

    // Search and filtering
    List<User> findByFullNameContainingIgnoreCase(String fullName);

    List<User> findByEmailContainingIgnoreCase(String email);

    // Date range queries
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<User> findByLastLoginAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Activity queries
    List<User> findByLastLoginAtAfter(LocalDateTime dateTime);

    List<User> findByLastLoginAtBefore(LocalDateTime dateTime);

    List<User> findByLastLoginAtIsNull(); // Never logged in

    // Department and job queries
    List<User> findByDepartment(String department);

    List<User> findByJobTitle(String jobTitle);

    // Bulk operations
    List<User> findByIdIn(List<Long> ids);

    // Statistics

}