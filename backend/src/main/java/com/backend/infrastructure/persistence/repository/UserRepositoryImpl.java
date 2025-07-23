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
        return userJpaRepository.findByEmailAndTenantId(email, tenantId);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
    
    @Override
    public boolean existsByEmailAndTenantId(String email, Long tenantId) {
        return userJpaRepository.existsByEmailAndTenantId(email, tenantId);
    }
    
    @Override
    public List<User> findByTenantId(Long tenantId) {
        return userJpaRepository.findByTenantId(tenantId);
    }
    
    @Override
    public List<User> findByTenantIdAndIsActive(Long tenantId, Boolean isActive) {
        return userJpaRepository.findByTenantIdAndIsActive(tenantId, isActive);
    }
    
    @Override
    public long countByTenantId(Long tenantId) {
        return userJpaRepository.countByTenantId(tenantId);
    }
    
    @Override
    public long countByTenantIdAndIsActive(Long tenantId, Boolean isActive) {
        return userJpaRepository.countByTenantIdAndIsActive(tenantId, isActive);
    }
    
    @Override
    public List<User> findByRole(UserRole role) {
        return userJpaRepository.findByRole(role);
    }
    
    @Override
    public List<User> findByTenantIdAndRole(Long tenantId, UserRole role) {
        return userJpaRepository.findByTenantIdAndRole(tenantId, role);
    }
    
    @Override
    public List<User> findByRoleIn(List<UserRole> roles) {
        return userJpaRepository.findByRoleIn(roles);
    }
    
    @Override
    public long countByTenantIdAndRole(Long tenantId, UserRole role) {
        return userJpaRepository.countByTenantIdAndRole(tenantId, role);
    }
    
    @Override
    public List<User> findByPreferredLanguage(Language language) {
        return userJpaRepository.findByPreferredLanguage(language);
    }
    
    @Override
    public List<User> findByTenantIdAndPreferredLanguage(Long tenantId, Language language) {
        return userJpaRepository.findByTenantIdAndPreferredLanguage(tenantId, language);
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
        return userJpaRepository.findByTenantIdAndFullNameContainingIgnoreCase(tenantId, fullName);
    }
    
    @Override
    public List<User> findByTenantIdAndEmailContainingIgnoreCase(Long tenantId, String email) {
        return userJpaRepository.findByTenantIdAndEmailContainingIgnoreCase(tenantId, email);
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
        return userJpaRepository.findByTenantIdAndCreatedAtBetween(tenantId, startDate, endDate);
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
        return userJpaRepository.findByTenantIdAndLastLoginAtIsNull(tenantId);
    }
    
    @Override
    public List<User> findByTenantIdAndDepartment(Long tenantId, String department) {
        return userJpaRepository.findByTenantIdAndDepartment(tenantId, department);
    }
    
    @Override
    public List<User> findByTenantIdAndJobTitle(Long tenantId, String jobTitle) {
        return userJpaRepository.findByTenantIdAndJobTitle(tenantId, jobTitle);
    }
    
    @Override
    public List<User> findByIdIn(List<Long> ids) {
        return userJpaRepository.findByIdIn(ids);
    }
    
    @Override
    public void deleteByTenantId(Long tenantId) {
        userJpaRepository.deleteByTenantId(tenantId);
    }
    
    @Override
    public long countByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return userJpaRepository.countByTenantIdAndCreatedAtBetween(tenantId, startDate, endDate);
    }
    
    @Override
    public long countByTenantIdAndLastLoginAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return userJpaRepository.countByTenantIdAndLastLoginAtBetween(tenantId, startDate, endDate);
    }
    
    @Override
    public long countByTenantIdAndLastLoginAtAfter(Long tenantId, LocalDateTime dateTime) {
        return userJpaRepository.countByTenantIdAndLastLoginAtAfter(tenantId, dateTime);
    }
}