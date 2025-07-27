package com.backend.presentation.dto.mapper;

import com.backend.domain.entity.User;
import com.backend.domain.entity.Tenant;
import com.backend.presentation.dto.request.CreateUserRequest;
import com.backend.presentation.dto.request.UpdateUserRequest;
import com.backend.presentation.dto.response.UserResponse;
import com.backend.presentation.dto.response.PasswordResetResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {
    
    public User toEntity(CreateUserRequest request) {
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(request.password()); // Raw password will be encoded in the service layer
        user.setFullName(request.fullName());
        user.setRole(request.role());
        user.setPreferredLanguage(request.preferredLanguage());
        user.setPhone(request.phone());
        user.setJobTitle(request.jobTitle());
        user.setDepartment(request.department());
        user.setTimezone(request.timezone());
        user.setIsActive(request.isActive() != null ? request.isActive() : true);
        
        // Set defaults
        user.setEmailVerified(false);
        user.setTwoFactorEnabled(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setPasswordChangedAt(LocalDateTime.now());
        
        return user;
    }
    
    public User toEntity(UpdateUserRequest request, User existingUser) {
        existingUser.setEmail(request.email());
        existingUser.setFullName(request.fullName());
        existingUser.setRole(request.role());
        existingUser.setPreferredLanguage(request.preferredLanguage());
        existingUser.setPhone(request.phone());
        existingUser.setJobTitle(request.jobTitle());
        existingUser.setDepartment(request.department());
        existingUser.setTimezone(request.timezone());
        existingUser.setIsActive(request.isActive() != null ? request.isActive() : true);
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        return existingUser;
    }
    
    public UserResponse toResponse(User user) {
        return toResponse(user, null);
    }
    
    public UserResponse toResponse(User user, Tenant tenant) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole(),
            user.getPreferredLanguage(),
            user.getTenantId(),
            tenant != null ? tenant.getCompanyName() : null,
            user.getIsActive(),
            user.getEmailVerified(),
            user.getTwoFactorEnabled(),
            user.isAccountLocked(),
            user.getPhone(),
            user.getJobTitle(),
            user.getDepartment(),
            user.getTimezone(),
            user.getAvatarUrl(),
            user.getLastLoginAt(),
            user.getLockedUntil(),
            user.getFailedLoginAttempts(),
            user.getPasswordChangedAt(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
    
    public PasswordResetResponse toPasswordResetResponse() {
        return new PasswordResetResponse(
            "Password has been reset successfully.",
            "A temporary password has been generated and will be sent to your registered email address. Please check your email and use the temporary password to login."
        );
    }
}