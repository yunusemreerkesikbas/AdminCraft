package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.UserRole;
import com.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    
    private final UserJpaRepository userJpaRepository;
    
    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }
    
    @Override
    public List<User> saveAll(Iterable<User> users) {
        return userJpaRepository.saveAll(users);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id);
    }
    
    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll();
    }
    
    @Override
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return userJpaRepository.existsById(id);
    }
    
    @Override
    public long count() {
        return userJpaRepository.count();
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email);
    }
    
    @Override
    public Optional<User> findByEmailAndTenantId(String email, Long tenantId) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailAndTenantId(String email, Long tenantId) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public List<User> findByTenantId(Long tenantId) {
        // In database-per-tenant model, all users in current DB belong to tenant
        return userJpaRepository.findAll();
    }

    @Override
    public List<User> findByTenantIdAndIsActive(Long tenantId, Boolean isActive) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.findByIsActive(isActive);
    }

    @Override
    public long countByTenantId(Long tenantId) {
        // In database-per-tenant model, all users in current DB belong to tenant
        return userJpaRepository.count();
    }

    @Override
    public long countByTenantIdAndIsActive(Long tenantId, Boolean isActive) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.countByIsActive(isActive);
    }
    
    @Override
    public List<User> findByRole(UserRole role) {
        return userJpaRepository.findByRole(role);
    }
    
    @Override
    public List<User> findByTenantIdAndRole(Long tenantId, UserRole role) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.findByRole(role);
    }

    @Override
    public List<User> findByRoleIn(List<UserRole> roles) {
        return userJpaRepository.findByRoleIn(roles);
    }

    @Override
    public long countByTenantIdAndRole(Long tenantId, UserRole role) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.countByRole(role);
    }

    @Override
    public List<User> findByPreferredLanguage(Language language) {
        return userJpaRepository.findByPreferredLanguage(language);
    }

    @Override
    public List<User> findByTenantIdAndPreferredLanguage(Long tenantId, Language language) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.findByPreferredLanguage(language);
    }
    
    @Override
    public List<User> findByIsActive(Boolean isActive) {
        return userJpaRepository.findByIsActive(isActive);
    }
    
    @Override
    public List<User> findByEmailVerified(Boolean emailVerified) {
        return userJpaRepository.findByEmailVerified(emailVerified);
    }
    
    @Override
    public List<User> findByTwoFactorEnabled(Boolean twoFactorEnabled) {
        return userJpaRepository.findByTwoFactorEnabled(twoFactorEnabled);
    }
    
    @Override
    public List<User> findByFailedLoginAttemptsGreaterThanEqual(Integer attempts) {
        return userJpaRepository.findByFailedLoginAttemptsGreaterThanEqual(attempts);
    }
    
    @Override
    public List<User> findByLockedUntilAfter(LocalDateTime dateTime) {
        return userJpaRepository.findByLockedUntilAfter(dateTime);
    }
    
    @Override
    public List<User> findByPasswordChangedAtBefore(LocalDateTime dateTime) {
        return userJpaRepository.findByPasswordChangedAtBefore(dateTime);
    }
    
    @Override
    public List<User> findByTenantIdAndFullNameContainingIgnoreCase(Long tenantId, String fullName) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.findByFullNameContainingIgnoreCase(fullName);
    }

    @Override
    public List<User> findByTenantIdAndEmailContainingIgnoreCase(Long tenantId, String email) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.findByEmailContainingIgnoreCase(email);
    }

    @Override
    public List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return userJpaRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public List<User> findByLastLoginAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return userJpaRepository.findByLastLoginAtBetween(startDate, endDate);
    }

    @Override
    public List<User> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.findByCreatedAtBetween(startDate, endDate);
    }
    
    @Override
    public List<User> findByLastLoginAtAfter(LocalDateTime dateTime) {
        return userJpaRepository.findByLastLoginAtAfter(dateTime);
    }
    
    @Override
    public List<User> findByLastLoginAtBefore(LocalDateTime dateTime) {
        return userJpaRepository.findByLastLoginAtBefore(dateTime);
    }
    
    @Override
    public List<User> findByTenantIdAndLastLoginAtIsNull(Long tenantId) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.findByLastLoginAtIsNull();
    }

    @Override
    public List<User> findByTenantIdAndDepartment(Long tenantId, String department) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.findByDepartment(department);
    }

    @Override
    public List<User> findByTenantIdAndJobTitle(Long tenantId, String jobTitle) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.findByJobTitle(jobTitle);
    }

    @Override
    public List<User> findByIdIn(List<Long> ids) {
        return userJpaRepository.findByIdIn(ids);
    }

    @Override
    public void deleteByTenantId(Long tenantId) {
        // In database-per-tenant model, delete all users in current database
        userJpaRepository.deleteAll();
    }

    @Override
    public long countByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.countByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public long countByTenantIdAndLastLoginAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.countByLastLoginAtBetween(startDate, endDate);
    }

    @Override
    public long countByTenantIdAndLastLoginAtAfter(Long tenantId, LocalDateTime dateTime) {
        // In database-per-tenant model, routing is handled by TenantContext
        return userJpaRepository.countByLastLoginAtAfter(dateTime);
    }
}