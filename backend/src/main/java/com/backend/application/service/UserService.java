package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.dto.CreateUserInput;
import com.backend.application.dto.UpdateUserInput;
import com.backend.domain.entity.User;
import com.backend.domain.enums.UserRole;

public interface UserService {

    // Basic CRUD operations
    User createUser(CreateUserInput input);

    Optional<User> getUserById(Long id);

    User updateUser(Long id, UpdateUserInput input);

    void deleteUser(Long id);

    List<User> getAllUsers();

    // Pagination and search
    Page<User> searchUsers(String search, Pageable pageable);

    // Authentication operations
    Optional<User> findByEmail(String email);

    boolean validateUser(String email, String password);

    User authenticate(String email, String password);

    void recordLoginAttempt(String email, String ipAddress, boolean success);

    User getCurrentUser();

    // Password management
    void changePassword(Long userId, String currentPassword, String newPassword);

    String resetPassword(Long userId);

    void updatePasswordHash(Long userId, String newPasswordHash);

    boolean isPasswordExpired(Long userId, int maxDays);

    // Account management
    User activateUser(Long userId);

    User deactivateUser(Long userId);

    void lockUser(Long userId, LocalDateTime until);

    void unlockUser(Long userId);

    void verifyEmail(Long userId);

    boolean isUserLocked(Long userId);

    // Tenant operations (Redundant in database-per-tenant)
    // Removed getUsersByTenantId, getActiveUsersByTenantId, countUsersByTenantId,
    // createTenantAdmin

    // Role management
    void assignRole(Long userId, UserRole role);

    List<User> getUsersByRole(UserRole role);

    boolean hasPermission(Long userId, UserRole.Permission permission);

    // Profile management
    User updateProfile(Long userId, String fullName, String phone, String jobTitle, String department);

    // Search and filtering
    List<User> searchUsersByTerm(String searchTerm);

    List<User> getUsersByDepartment(String department);

    List<User> getUsersByJobTitle(String jobTitle);

    // Statistics and reporting
    long getTotalUsersCount();

    long getActiveUsersCount();

    long getUsersCountByRole(UserRole role);

    List<User> getRecentlyActiveUsers(int limit);

    List<User> getUsersNeverLoggedIn();

    // Security operations
    void enableTwoFactor(Long userId);

    void disableTwoFactor(Long userId);

    boolean isTwoFactorEnabled(Long userId);

    void resetFailedLoginAttempts(Long userId);

    List<User> getLockedUsers();

    // Bulk operations
    void bulkUpdateRole(List<Long> userIds, UserRole role);

    void bulkActivate(List<Long> userIds);

    void bulkDeactivate(List<Long> userIds);

    // Validation
    boolean existsByEmail(String email);

    // Access control methods for security
    boolean hasAccessToUser(String currentUserEmail, Long userId);

    boolean hasAccessToUserByEmail(String currentUserEmail, String targetEmail);
}