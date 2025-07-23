package com.backend.domain.repository;

import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    
    // Basic CRUD operations
    User save(User user);
    List<User> saveAll(Iterable<User> users);
    Optional<User> findById(Long id);
    List<User> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();
    
    // Authentication queries
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndTenantId(String email, Long tenantId);
    boolean existsByEmail(String email);
    boolean existsByEmailAndTenantId(String email, Long tenantId);
    
    // Tenant-specific queries
    List<User> findByTenantId(Long tenantId);
    List<User> findByTenantIdAndIsActive(Long tenantId, Boolean isActive);
    long countByTenantId(Long tenantId);
    long countByTenantIdAndIsActive(Long tenantId, Boolean isActive);
    
    // Role-based queries
    List<User> findByRole(UserRole role);
    List<User> findByTenantIdAndRole(Long tenantId, UserRole role);
    List<User> findByRoleIn(List<UserRole> roles);
    long countByTenantIdAndRole(Long tenantId, UserRole role);
    
    // Language-based queries
    List<User> findByPreferredLanguage(Language language);
    List<User> findByTenantIdAndPreferredLanguage(Long tenantId, Language language);
    
    // Status queries
    List<User> findByIsActive(Boolean isActive);
    List<User> findByEmailVerified(Boolean emailVerified);
    List<User> findByTwoFactorEnabled(Boolean twoFactorEnabled);
    
    // Account security queries
    List<User> findByFailedLoginAttemptsGreaterThanEqual(Integer attempts);
    List<User> findByLockedUntilAfter(LocalDateTime dateTime);
    List<User> findByPasswordChangedAtBefore(LocalDateTime dateTime);
    
    // Search and filtering
    List<User> findByTenantIdAndFullNameContainingIgnoreCase(Long tenantId, String fullName);
    List<User> findByTenantIdAndEmailContainingIgnoreCase(Long tenantId, String email);
    
    // Date range queries
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<User> findByLastLoginAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<User> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Activity queries
    List<User> findByLastLoginAtAfter(LocalDateTime dateTime);
    List<User> findByLastLoginAtBefore(LocalDateTime dateTime);
    List<User> findByTenantIdAndLastLoginAtIsNull(Long tenantId); // Never logged in
    
    // Department and job queries
    List<User> findByTenantIdAndDepartment(Long tenantId, String department);
    List<User> findByTenantIdAndJobTitle(Long tenantId, String jobTitle);
    
    // Bulk operations
    List<User> findByIdIn(List<Long> ids);
    void deleteByTenantId(Long tenantId);
    
    // Statistics
    long countByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    long countByTenantIdAndLastLoginAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    long countByTenantIdAndLastLoginAtAfter(Long tenantId, LocalDateTime dateTime);
}