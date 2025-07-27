package com.backend.presentation.controller;

import com.backend.application.service.UserService;
import com.backend.application.service.TenantService;
import com.backend.domain.entity.User;
import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.UserRole;
import com.backend.presentation.dto.mapper.UserMapper;
import com.backend.presentation.dto.request.CreateUserRequest;
import com.backend.presentation.dto.request.UpdateUserRequest;
import com.backend.presentation.dto.request.ChangePasswordRequest;
import com.backend.presentation.dto.response.UserResponse;
import com.backend.presentation.dto.response.PasswordResetResponse;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageSource messageSource;

    private static final String SECURE_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%^&*";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            
            // Convert DTO to Entity
            User user = userMapper.toEntity(request);
            
            // Set tenant ID from context (would normally come from JWT token)
            user.setTenantId(1L); // TODO: Get from security context
            
            // Create user
            User savedUser = userService.createUser(user);
            
            // Get tenant for response
            Optional<Tenant> tenant = tenantService.getTenantEntityById(savedUser.getTenantId());
            
            UserResponse response = userMapper.toResponse(savedUser, tenant.orElse(null));
            
            String message = messageSource.getMessage("user.created.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("user.create.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isEmpty()) {
                String message = messageSource.getMessage("user.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
            
            User user = userOpt.get();
            
            // Get tenant for response
            Optional<Tenant> tenant = tenantService.getTenantEntityById(user.getTenantId());
            
            UserResponse response = userMapper.toResponse(user, tenant.orElse(null));
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("user.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean active,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            Long tenantId = 1L; // TODO: Get from security context
            
            List<User> users;
            if (role != null) {
                users = userService.getUsersByTenantIdAndRole(tenantId, role);
            } else if (active != null && active) {
                users = userService.getActiveUsersByTenantId(tenantId);
            } else {
                users = userService.getUsersByTenantId(tenantId);
            }
            
            // Get tenant for response
            Optional<Tenant> tenant = tenantService.getTenantEntityById(tenantId);
            
            List<UserResponse> responses = users.stream()
                .map(user -> userMapper.toResponse(user, tenant.orElse(null)))
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception ex) {
            String message = messageSource.getMessage("user.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            
            Optional<User> existingUserOpt = userService.getUserById(id);
            if (existingUserOpt.isEmpty()) {
                String message = messageSource.getMessage("user.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
            
            User existingUser = existingUserOpt.get();
            User updatedUser = userMapper.toEntity(request, existingUser);
            
            User savedUser = userService.updateUser(updatedUser);
            
            // Get tenant for response
            Optional<Tenant> tenant = tenantService.getTenantEntityById(savedUser.getTenantId());
            
            UserResponse response = userMapper.toResponse(savedUser, tenant.orElse(null));
            
            String message = messageSource.getMessage("user.updated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("user.update.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            userService.deleteUser(id);
            String message = messageSource.getMessage("user.deleted.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            String message = messageSource.getMessage("user.delete.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            
            userService.activateUser(id);
            
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Optional<Tenant> tenant = tenantService.getTenantEntityById(user.getTenantId());
                UserResponse response = userMapper.toResponse(user, tenant.orElse(null));
                
                String message = messageSource.getMessage("user.activated.success", null, Locale.forLanguageTag(languageCode));
                return ResponseEntity.ok(ApiResponse.success(message, response));
            }
            
            String message = messageSource.getMessage("user.activated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            String message = messageSource.getMessage("user.activate.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            
            userService.deactivateUser(id);
            
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Optional<Tenant> tenant = tenantService.getTenantEntityById(user.getTenantId());
                UserResponse response = userMapper.toResponse(user, tenant.orElse(null));
                
                String message = messageSource.getMessage("user.deactivated.success", null, Locale.forLanguageTag(languageCode));
                return ResponseEntity.ok(ApiResponse.success(message, response));
            }
            
            String message = messageSource.getMessage("user.deactivated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            String message = messageSource.getMessage("user.deactivate.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            userService.changePassword(id, request.currentPassword(), request.newPassword());
            String message = messageSource.getMessage("user.password.changed.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            String message = messageSource.getMessage("user.password.change.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<PasswordResetResponse>> resetPassword(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Generate cryptographically secure temporary password
            String tempPassword = generateSecureTemporaryPassword();
            
            // Get user to obtain email for proper reset method
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isEmpty()) {
                String message = messageSource.getMessage("user.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
            
            User user = userOpt.get();
            
            // Use proper password reset method that handles hashing
            userService.resetPasswordWithNewPassword(user.getEmail(), user.getTenantId(), tempPassword);
            
            PasswordResetResponse response = userMapper.toPasswordResetResponse(tempPassword);
            
            String message = messageSource.getMessage("user.password.reset.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            String message = messageSource.getMessage("user.password.reset.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    /**
     * Generates a cryptographically secure temporary password
     * @return A secure 16-character password
     */
    private String generateSecureTemporaryPassword() {
        StringBuilder password = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(SECURE_PASSWORD_CHARS.length());
            password.append(SECURE_PASSWORD_CHARS.charAt(randomIndex));
        }
        return password.toString();
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @RequestParam String query,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            Long tenantId = 1L; // TODO: Get from security context
            
            List<User> users = userService.searchUsers(tenantId, query);
            
            // Get tenant for response
            Optional<Tenant> tenant = tenantService.getTenantEntityById(tenantId);
            
            List<UserResponse> responses = users.stream()
                .map(user -> userMapper.toResponse(user, tenant.orElse(null)))
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception ex) {
            String message = messageSource.getMessage("user.search.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }
}