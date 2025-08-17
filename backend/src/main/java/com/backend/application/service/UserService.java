package com.backend.application.service;

import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserService {
    
    // Basic CRUD operations
    User createUser(User user);
    Optional<User> getUserById(Long id);
    User updateUser(User user);
    void deleteUser(Long id);
    List<User> getAllUsers();
    
    // Authentication operations
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndTenantId(String email, Long tenantId);
    boolean validateUser(String email, String password, Long tenantId);
    User authenticate(String email, String password, Long tenantId);
    void recordLoginAttempt(String email, String ipAddress, boolean success, Long tenantId);
    User getCurrentUser();
    
    // Password management
    void changePassword(Long userId, String currentPassword, String newPassword);
    void resetPassword(String email, Long tenantId);
    void updatePasswordHash(Long userId, String newPasswordHash);
    boolean isPasswordExpired(Long userId, int maxDays);
    
    // Account management
    User activateUser(Long userId);
    User deactivateUser(Long userId);
    void lockUser(Long userId, LocalDateTime until);
    void unlockUser(Long userId);
    void verifyEmail(Long userId);
    boolean isUserLocked(Long userId);
    
    // Tenant operations
    List<User> getUsersByTenantId(Long tenantId);
    List<User> getActiveUsersByTenantId(Long tenantId);
    long countUsersByTenantId(Long tenantId);
    User createTenantAdmin(String email, String fullName, Long tenantId, Language preferredLanguage);
    
    // Role management
    void assignRole(Long userId, UserRole role);
    List<User> getUsersByRole(UserRole role);
    List<User> getUsersByTenantIdAndRole(Long tenantId, UserRole role);
    boolean hasPermission(Long userId, UserRole.Permission permission);
    
    // Profile management
    User updateProfile(Long userId, String fullName, String phone, String jobTitle, String department);
    User updatePreferences(Long userId, Language preferredLanguage, String timezone);
    User updateUserLanguage(Long userId, Language language);
    void updateAvatar(Long userId, String avatarUrl);
    
    // Search and filtering
    List<User> searchUsers(Long tenantId, String searchTerm);
    List<User> getUsersByDepartment(Long tenantId, String department);
    List<User> getUsersByJobTitle(Long tenantId, String jobTitle);
    
    // Statistics and reporting
    long getTotalUsersCount();
    long getActiveUsersCount(Long tenantId);
    long getUsersCountByRole(Long tenantId, UserRole role);
    List<User> getRecentlyActiveUsers(Long tenantId, int limit);
    List<User> getUsersNeverLoggedIn(Long tenantId);
    
    // Security operations
    void enableTwoFactor(Long userId);
    void disableTwoFactor(Long userId);
    boolean isTwoFactorEnabled(Long userId);
    void resetFailedLoginAttempts(Long userId);
    List<User> getLockedUsers(Long tenantId);
    
    // Bulk operations
    void bulkUpdateRole(List<Long> userIds, UserRole role);
    void bulkActivate(List<Long> userIds);
    void bulkDeactivate(List<Long> userIds);
    void deleteUsersByTenantId(Long tenantId);
    
    // Validation
    boolean existsByEmail(String email);
    boolean existsByEmailAndTenantId(String email, Long tenantId);
    boolean canUserAccessTenant(Long userId, Long tenantId);
    
    // Access control methods for security
    boolean hasAccessToUser(String currentUserEmail, Long userId);
    boolean hasAccessToUserByEmail(String currentUserEmail, String targetEmail);
}