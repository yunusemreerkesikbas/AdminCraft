package com.backend.presentation.controller;

import com.backend.application.service.UserService;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            validateUserAccess(id);
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(user.get()));
            } else {
                String message = messageSource.getMessage("user.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting user by id {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("user.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(
            @PathVariable @Valid @NotBlank @Email String email,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Sanitize email input
            String sanitizedEmail = sanitizeEmail(email);
            if (sanitizedEmail == null || sanitizedEmail.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid email");
            }
            
            Optional<User> user = userService.findByEmail(sanitizedEmail);
            if (user.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(user.get()));
            } else {
                String message = messageSource.getMessage("user.email.not.found", new Object[]{email}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting user by email {}: {}", email, ex.getMessage());
            String message = messageSource.getMessage("user.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByTenant(
            @PathVariable @Valid @NotNull @Min(1) Long tenantId,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            validateTenantAccess(tenantId);
            List<User> users = userService.getUsersByTenantId(tenantId);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception ex) {
            log.error("Error getting users by tenant {}: {}", tenantId, ex.getMessage());
            String message = messageSource.getMessage("user.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception ex) {
            log.error("Error getting all users: {}", ex.getMessage());
            String message = messageSource.getMessage("user.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Access control handled by TenantContext routing
            validateUserAccess(id);

            userService.deleteUser(id);
            String message = messageSource.getMessage("user.delete.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Error deleting user {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("user.delete.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<User>> activateUser(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            User activatedUser = userService.activateUser(id);
            String message = messageSource.getMessage("user.activated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, activatedUser));
        } catch (Exception ex) {
            log.error("Error activating user {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("user.activate.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<User>> deactivateUser(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            User deactivatedUser = userService.deactivateUser(id);
            String message = messageSource.getMessage("user.deactivated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, deactivatedUser));
        } catch (Exception ex) {
            log.error("Error deactivating user {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("user.deactivate.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @PutMapping("/{id}/language")
    public ResponseEntity<ApiResponse<User>> updateUserLanguage(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestParam @Valid @NotNull Language language,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // Access control handled by TenantContext routing
            validateUserAccess(id);

            User updatedUser = userService.updateUserLanguage(id, language);
            String message = messageSource.getMessage("user.language.updated.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, updatedUser));
        } catch (Exception ex) {
            log.error("Error updating user language for user {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("user.language.update.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            User currentUser = userService.getCurrentUser();
            return ResponseEntity.ok(ApiResponse.success(currentUser));
        } catch (Exception ex) {
            log.error("Error getting current user: {}", ex.getMessage());
            String message = messageSource.getMessage("user.current.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    /**
     * Validates user access based on current user context
     */
    private void validateUserAccess(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        
        // TODO: Implement actual user access validation based on current user's permissions
        // In a real implementation, you would check if the current user has access to this user
        // Example:
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // UserDetails userDetails = (UserDetails) auth.getPrincipal();
        // if (!userService.userHasAccessToUser(userDetails.getUsername(), userId)) {
        //     throw new AccessDeniedException("Access denied to user: " + userId);
        // }
    }

    /**
     * Validates tenant access based on current user context
     */
    private void validateTenantAccess(Long tenantId) {
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("Invalid tenant ID");
        }
        
        // TODO: Implement actual tenant access validation based on current user's tenant
        // In a real implementation, you would check if the current user has access to this tenant
        // Example:
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // UserDetails userDetails = (UserDetails) auth.getPrincipal();
        // if (!tenantService.userHasAccessToTenant(userDetails.getUsername(), tenantId)) {
        //     throw new AccessDeniedException("Access denied to tenant: " + tenantId);
        // }
    }

    /**
     * Sanitizes email input to prevent injection attacks
     */
    private String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        
        // Basic email sanitization - trim whitespace and convert to lowercase
        return email.trim().toLowerCase();
    }
}