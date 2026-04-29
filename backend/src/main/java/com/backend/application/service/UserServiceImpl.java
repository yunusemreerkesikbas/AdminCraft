package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.CreateUserInput;
import com.backend.application.dto.UpdateUserInput;
import com.backend.domain.entity.User;
import com.backend.domain.entity.VerificationToken;
import com.backend.domain.enums.UserRole;
import com.backend.domain.exception.UserNotFoundException;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.UserRepository;
import com.backend.shared.common.SecurityHelper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@lombok.RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;
    private final EmailService emailService;
    private final OtpService otpService;
    private final TenantContextPort tenantContext;
    private final SecurityHelper securityHelper;

    private String resolveFullName(String firstName, String lastName, String email) {
        boolean hasFirstName = firstName != null && !firstName.trim().isEmpty();
        boolean hasLastName = lastName != null && !lastName.trim().isEmpty();
        if (hasFirstName && hasLastName) {
            return (firstName + " " + lastName).trim();
        }
        if (hasFirstName) {
            return firstName.trim();
        }
        if (hasLastName) {
            return lastName.trim();
        }
        return email;
    }

    @Override
    public User createUser(CreateUserInput input) {
        if (!tenantContext.isSet()) {
            throw new IllegalStateException("Tenant context is required to create a user");
        }
        log.debug("Creating new user with email: {}", input.email());

        if (userRepository.existsByEmail(input.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        String tempPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());

        User user = new User();
        user.setEmail(input.email());
        user.setPasswordHash(tempPasswordHash);
        user.setRole(input.role());
        user.setFirstName(input.firstName());
        user.setLastName(input.lastName());
        user.setFullName(resolveFullName(input.firstName(), input.lastName(), input.email()));
        user.setPhone(input.phone());
        user.setJobTitle(input.jobTitle());
        user.setDepartment(input.department());
        user.setIsActive(input.isActive());
        user.setEmailVerified(false);
        user.setNotes(input.notes());

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        try {
            var tokenResult = otpService.createEmailVerificationToken(
                    savedUser,
                    input.ipAddress(),
                    input.userAgent());
            emailService.sendEmailVerificationEmail(
                    savedUser.getEmail(),
                    tokenResult.plainToken(),
                    tenantContext.getSubdomain(),
                    input.language());
            log.info("Verification email sent to: {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", savedUser.getEmail(), e);
        }

        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User updateUser(Long id, UpdateUserInput input) {
        log.debug("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (input.email() != null) {
            userRepository.findByEmail(input.email()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Email already exists");
                }
            });
            user.setEmail(input.email());
        }
        // role field intentionally ignored here — use updateUserRole() (SEC-109)
        if (input.firstName() != null)
            user.setFirstName(input.firstName());
        if (input.lastName() != null)
            user.setLastName(input.lastName());
        if (input.firstName() != null || input.lastName() != null || input.email() != null) {
            user.setFullName(resolveFullName(user.getFirstName(), user.getLastName(), user.getEmail()));
        }
        if (input.phone() != null)
            user.setPhone(input.phone());
        if (input.jobTitle() != null)
            user.setJobTitle(input.jobTitle());
        if (input.department() != null)
            user.setDepartment(input.department());
        if (input.isActive() != null)
            user.setIsActive(input.isActive());
        if (input.notes() != null)
            user.setNotes(input.notes());

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());
        return updatedUser;
    }

    @Override
    public void updateUserRole(Long id, UserRole role) {
        Long callerId = securityHelper.getCurrentUserIdOrNull();
        if (id.equals(callerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Cannot change own role");
        }
        if (role == UserRole.SUPER_ADMIN) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "SUPER_ADMIN role cannot be assigned via tenant API");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setRole(role);
        userRepository.save(user);
        log.info("Role updated to {} for user ID: {}", role, id);
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
    public Page<User> searchUsers(String search, Pageable pageable) {
        return userRepository.searchUsers(search, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.debug("User not found with email: {}", email);
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
    public User authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(email);
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
    public void recordLoginAttempt(String email, String ipAddress, boolean success) {
        Optional<User> userOpt = userRepository.findByEmail(email);

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
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to get current user", ex);
        }
    }

    @Override
    public void updatePasswordHash(Long userId, String newPasswordHash) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.changePassword(newPasswordHash);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPasswordExpired(Long userId, int maxDays) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getPasswordChangedAt() == null) {
            return true;
        }

        return user.getPasswordChangedAt().isBefore(LocalDateTime.now().minusDays(maxDays));
    }

    @Override
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setIsActive(true);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);

        User savedUser = userRepository.save(user);
        log.info("User activated: {}", userId);
        return savedUser;
    }

    @Override
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setIsActive(false);
        User savedUser = userRepository.save(user);
        log.info("User deactivated: {}", userId);
        return savedUser;
    }

    @Override
    public void lockUser(Long userId, LocalDateTime until) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setLockedUntil(until);
        userRepository.save(user);
        log.info("User locked until {}: {}", until, userId);
    }

    @Override
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        log.info("User unlocked: {}", userId);
    }

    @Override
    public void verifyEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserLocked(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return user.isAccountLocked();
    }

    @Override
    public void assignRole(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

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
    public boolean hasPermission(Long userId, UserRole.Permission permission) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return user.getRole().hasPermission(permission);
    }

    @Override
    public User updateProfile(Long userId, String phone, String jobTitle, String department) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setPhone(phone);
        user.setJobTitle(jobTitle);
        user.setDepartment(department);
        user.setFullName(resolveFullName(user.getFirstName(), user.getLastName(), user.getEmail()));

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsersByTerm(String searchTerm) {
        List<User> nameResults = userRepository.findByFullNameContainingIgnoreCase(searchTerm);
        List<User> emailResults = userRepository.findByEmailContainingIgnoreCase(searchTerm);
        nameResults.addAll(emailResults);
        return nameResults.stream().distinct().toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByDepartment(String department) {
        return userRepository.findByDepartment(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByJobTitle(String jobTitle) {
        return userRepository.findByJobTitle(jobTitle);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalUsersCount() {
        return userRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveUsersCount() {
        return userRepository.countByIsActive(true);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUsersCountByRole(UserRole role) {
        return userRepository.countByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getRecentlyActiveUsers(int limit) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        return userRepository.findByLastLoginAtAfter(cutoffDate)
                .stream()
                .limit(limit)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersNeverLoggedIn() {
        return userRepository.findByLastLoginAtIsNull();
    }

    @Override
    public void resetFailedLoginAttempts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getLockedUsers() {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findByLockedUntilAfter(now);
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
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccessToUser(String currentUserEmail, Long userId) {
        log.debug("Checking user access for {} to user {}", currentUserEmail, userId);

        Optional<User> currentUser = userRepository.findByEmail(currentUserEmail);
        if (currentUser.isEmpty()) {
            log.warn("Current user not found: {}", currentUserEmail);
            return false;
        }

        User user = currentUser.get();

        if (user.getRole().name().equals("SUPER_ADMIN")) {
            log.debug("User {} has SUPER_ADMIN role, granting access to user {}", currentUserEmail, userId);
            return true;
        }

        Optional<User> targetUser = userRepository.findById(userId);
        if (targetUser.isEmpty()) {
            log.warn("Target user not found: {}", userId);
            return false;
        }

        log.debug("User {} access to user {}: true (same tenant DB)", currentUserEmail, userId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccessToUserByEmail(String currentUserEmail, String targetEmail) {
        log.debug("Checking user access for {} to user with email {}", currentUserEmail, targetEmail);

        Optional<User> currentUser = userRepository.findByEmail(currentUserEmail);
        if (currentUser.isEmpty()) {
            log.warn("Current user not found: {}", currentUserEmail);
            return false;
        }

        User user = currentUser.get();

        if (user.getRole().name().equals("SUPER_ADMIN")) {
            log.debug("User {} has SUPER_ADMIN role, granting access to user {}", currentUserEmail, targetEmail);
            return true;
        }

        Optional<User> targetUser = userRepository.findByEmail(targetEmail);
        if (targetUser.isEmpty()) {
            log.warn("Target user not found: {}", targetEmail);
            return false;
        }

        log.debug("User {} access to user {}: true (same tenant DB)", currentUserEmail, targetEmail);
        return true;
    }
}
