package com.backend.application.service;

import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.UserRole;
import com.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Checks if a string is a valid BCrypt hash by verifying the complete structure.
     * BCrypt hashes must follow the pattern: $2[ayb]$XX$[53 chars of Base64]
     * where XX is a two-digit cost factor.
     * 
     * @param hash the string to check
     * @return true if the string is a properly formatted BCrypt hash
     */
    private boolean isBCryptHash(String hash) {
        if (hash == null || hash.length() != 60) {
            return false;
        }
        
        // Validate BCrypt hash structure: $2[ayb]$XX$[53 Base64 chars]
        return hash.matches("^\\$2[ayb]\\$[0-9]{2}\\$[A-Za-z0-9./]{53}$");
    }
    
    @Override
    public User createUser(User user) {
        log.debug("Creating new user with email: {}", user.getEmail());
        
        // Validate unique email for tenant
        if (userRepository.existsByEmailAndTenantId(user.getEmail(), user.getTenantId())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists for this tenant");
        }
        
        // Hash password if provided
        if (user.getPasswordHash() != null && !isBCryptHash(user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        
        // Set defaults
        if (user.getRole() == null) {
            user.setRole(UserRole.VIEWER);
        }
        if (user.getPreferredLanguage() == null) {
            user.setPreferredLanguage(Language.TR);
        }
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        if (user.getEmailVerified() == null) {
            user.setEmailVerified(false);
        }
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        return savedUser;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public User updateUser(User user) {
        log.debug("Updating user with ID: {}", user.getId());
        
        User existingUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + user.getId()));
        
        // Check email uniqueness if email is being changed
        if (!existingUser.getEmail().equals(user.getEmail()) &&
            userRepository.existsByEmailAndTenantId(user.getEmail(), user.getTenantId())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists for this tenant");
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());
        return updatedUser;
    }
    
    @Override
    public void deleteUser(Long id) {
        log.debug("Deleting user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        
        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmailAndTenantId(String email, Long tenantId) {
        return userRepository.findByEmailAndTenantId(email, tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateUser(String email, String password, Long tenantId) {
        Optional<User> userOpt = userRepository.findByEmailAndTenantId(email, tenantId);
        
        if (userOpt.isEmpty()) {
            log.debug("User not found with email: {} for tenant: {}", email, tenantId);
            return false;
        }
        
        User user = userOpt.get();
        
        if (!user.canLogin()) {
            log.debug("User cannot login: {}", email);
            return false;
        }
        
        boolean passwordMatches = passwordEncoder.matches(password, user.getPasswordHash());
        log.debug("Password validation for user {}: {}", email, passwordMatches);
        
        return passwordMatches;
    }
    
    @Override
    public User authenticate(String email, String password, Long tenantId) {
        Optional<User> userOpt = userRepository.findByEmailAndTenantId(email, tenantId);
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        User user = userOpt.get();
        
        if (!user.canLogin()) {
            throw new IllegalStateException("User account is locked or inactive");
        }
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.recordFailedLogin();
            userRepository.save(user);
            throw new IllegalArgumentException("Invalid password");
        }
        
        return user;
    }
    
    @Override
    public void recordLoginAttempt(String email, String ipAddress, boolean success, Long tenantId) {
        Optional<User> userOpt = userRepository.findByEmailAndTenantId(email, tenantId);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (success) {
                user.recordSuccessfulLogin(ipAddress);
                log.info("Successful login recorded for user: {}", email);
            } else {
                user.recordFailedLogin();
                log.warn("Failed login recorded for user: {}", email);
            }
            
            userRepository.save(user);
        }
    }
    
    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedNewPassword);
        
        userRepository.save(user);
        log.info("Password changed for user ID: {}", userId);
    }
    
    @Override
    public void resetPassword(String email, Long tenantId) {
        User user = userRepository.findByEmailAndTenantId(email, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Generate temporary password
        String tempPassword = UUID.randomUUID().toString().substring(0, 12);
        String encodedPassword = passwordEncoder.encode(tempPassword);
        
        user.changePassword(encodedPassword);
        userRepository.save(user);
        
        // TODO: Send email with temporary password
        log.info("Password reset for user: {}", email);
    }
    
    @Override
    public void updatePasswordHash(Long userId, String newPasswordHash) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.changePassword(newPasswordHash);
        userRepository.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isPasswordExpired(Long userId, int maxDays) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getPasswordChangedAt() == null) {
            return true;
        }
        
        return user.getPasswordChangedAt().isBefore(LocalDateTime.now().minusDays(maxDays));
    }
    
    @Override
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setIsActive(true);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        
        userRepository.save(user);
        log.info("User activated: {}", userId);
    }
    
    @Override
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", userId);
    }
    
    @Override
    public void lockUser(Long userId, LocalDateTime until) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setLockedUntil(until);
        userRepository.save(user);
        log.info("User locked until {}: {}", until, userId);
    }
    
    @Override
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        log.info("User unlocked: {}", userId);
    }
    
    @Override
    public void verifyEmail(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified for user: {}", userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isUserLocked(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return user.isAccountLocked();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByTenantId(Long tenantId) {
        return userRepository.findByTenantId(tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getActiveUsersByTenantId(Long tenantId) {
        return userRepository.findByTenantIdAndIsActive(tenantId, true);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countUsersByTenantId(Long tenantId) {
        return userRepository.countByTenantId(tenantId);
    }
    
    @Override
    public User createTenantAdmin(String email, String fullName, Long tenantId, Language preferredLanguage) {
        User admin = new User();
        admin.setEmail(email);
        admin.setFullName(fullName);
        admin.setTenantId(tenantId);
        admin.setRole(UserRole.TENANT_ADMIN);
        admin.setPreferredLanguage(preferredLanguage);
        admin.setIsActive(true);
        admin.setEmailVerified(true);
        
        // Generate temporary password
        String tempPassword = UUID.randomUUID().toString().substring(0, 12);
        admin.setPasswordHash(passwordEncoder.encode(tempPassword));
        
        User createdAdmin = userRepository.save(admin);
        
        // TODO: Send welcome email with temporary password
        log.info("Tenant admin created for tenant {}: {}", tenantId, email);
        
        return createdAdmin;
    }
    
    @Override
    public void assignRole(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setRole(role);
        userRepository.save(user);
        log.info("Role {} assigned to user: {}", role, userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByTenantIdAndRole(Long tenantId, UserRole role) {
        return userRepository.findByTenantIdAndRole(tenantId, role);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, UserRole.Permission permission) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return user.getRole().hasPermission(permission);
    }
    
    @Override
    public User updateProfile(Long userId, String fullName, String phone, String jobTitle, String department) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setJobTitle(jobTitle);
        user.setDepartment(department);
        
        return userRepository.save(user);
    }
    
    @Override
    public User updatePreferences(Long userId, Language preferredLanguage, String timezone) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setPreferredLanguage(preferredLanguage);
        // TODO: Add timezone field to User entity if needed
        
        return userRepository.save(user);
    }
    
    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(Long tenantId, String searchTerm) {
        // Search in both full name and email
        List<User> nameResults = userRepository.findByTenantIdAndFullNameContainingIgnoreCase(tenantId, searchTerm);
        List<User> emailResults = userRepository.findByTenantIdAndEmailContainingIgnoreCase(tenantId, searchTerm);
        
        // Combine results and remove duplicates
        nameResults.addAll(emailResults);
        return nameResults.stream().distinct().toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByDepartment(Long tenantId, String department) {
        return userRepository.findByTenantIdAndDepartment(tenantId, department);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByJobTitle(Long tenantId, String jobTitle) {
        return userRepository.findByTenantIdAndJobTitle(tenantId, jobTitle);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getTotalUsersCount() {
        return userRepository.count();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getActiveUsersCount(Long tenantId) {
        return userRepository.countByTenantIdAndIsActive(tenantId, true);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getUsersCountByRole(Long tenantId, UserRole role) {
        return userRepository.countByTenantIdAndRole(tenantId, role);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getRecentlyActiveUsers(Long tenantId, int limit) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        return userRepository.findByLastLoginAtAfter(cutoffDate)
                          .stream()
                          .filter(user -> user.getTenantId().equals(tenantId))
                          .limit(limit)
                          .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersNeverLoggedIn(Long tenantId) {
        return userRepository.findByTenantIdAndLastLoginAtIsNull(tenantId);
    }
    
    @Override
    public void enableTwoFactor(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        log.info("Two-factor authentication enabled for user: {}", userId);
    }
    
    @Override
    public void disableTwoFactor(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
        log.info("Two-factor authentication disabled for user: {}", userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isTwoFactorEnabled(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return user.getTwoFactorEnabled();
    }
    
    @Override
    public void resetFailedLoginAttempts(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getLockedUsers(Long tenantId) {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findByLockedUntilAfter(now)
                          .stream()
                          .filter(user -> user.getTenantId().equals(tenantId))
                          .toList();
    }
    
    @Override
    @Transactional(timeout = 30)
    public void bulkUpdateRole(List<Long> userIds, UserRole role) {
        List<User> users = userRepository.findByIdIn(userIds);
        
        for (User user : users) {
            user.setRole(role);
        }
        
        userRepository.saveAll(users);
        log.info("Bulk role update completed for {} users", users.size());
    }
    
    @Override
    @Transactional(timeout = 30)
    public void bulkActivate(List<Long> userIds) {
        List<User> users = userRepository.findByIdIn(userIds);
        
        for (User user : users) {
            user.setIsActive(true);
            user.setLockedUntil(null);
        }
        
        userRepository.saveAll(users);
        log.info("Bulk activation completed for {} users", users.size());
    }
    
    @Override
    @Transactional(timeout = 30)
    public void bulkDeactivate(List<Long> userIds) {
        List<User> users = userRepository.findByIdIn(userIds);
        
        for (User user : users) {
            user.setIsActive(false);
        }
        
        userRepository.saveAll(users);
        log.info("Bulk deactivation completed for {} users", users.size());
    }
    
    @Override
    @Transactional(timeout = 60)
    public void deleteUsersByTenantId(Long tenantId) {
        userRepository.deleteByTenantId(tenantId);
        log.info("All users deleted for tenant: {}", tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmailAndTenantId(String email, Long tenantId) {
        return userRepository.existsByEmailAndTenantId(email, tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canUserAccessTenant(Long userId, Long tenantId) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Super admin can access any tenant
        if (user.isSuperAdmin()) {
            return true;
        }
        
        // Regular users can only access their own tenant
        return user.getTenantId().equals(tenantId);
    }
}