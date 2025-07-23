package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {
    
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
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND " +
           "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<User> findByTenantIdAndFullNameContainingIgnoreCase(@Param("tenantId") Long tenantId, 
                                                           @Param("searchTerm") String searchTerm);
    
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<User> findByTenantIdAndEmailContainingIgnoreCase(@Param("tenantId") Long tenantId, 
                                                        @Param("email") String email);
    
    // Date range queries
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<User> findByLastLoginAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<User> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Activity queries
    List<User> findByLastLoginAtAfter(LocalDateTime dateTime);
    List<User> findByLastLoginAtBefore(LocalDateTime dateTime);
    List<User> findByTenantIdAndLastLoginAtIsNull(Long tenantId);
    
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