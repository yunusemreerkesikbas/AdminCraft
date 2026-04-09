package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.CreateUserInput;
import com.backend.application.dto.SecuritySettingsResult;
import com.backend.application.dto.UpdateUserInput;
import com.backend.application.service.AuthenticationService;
import com.backend.application.service.SecuritySettingsService;
import com.backend.application.service.UserService;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TwoFactorPolicy;
import com.backend.domain.exception.UserNotFoundException;
import com.backend.domain.port.TenantContextPort;
import com.backend.presentation.dto.request.CreateUserRequest;
import com.backend.presentation.dto.request.UpdateUserRequest;
import com.backend.presentation.dto.response.PageableResponse;
import com.backend.presentation.dto.response.SortConfig;
import com.backend.presentation.dto.response.UserResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.RequestUtils;
import com.backend.shared.common.SortParseUtil;
import com.backend.shared.config.SortableFieldsConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
@Tag(name = "Users", description = "Tenant user management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

        private final UserService userService;
        private final AuthenticationService authenticationService;
        private final SecuritySettingsService securitySettingsService;
        private final TenantContextPort tenantContext;
        private final MessageSource messageSource;

        @GetMapping
        @Operation(summary = "List users", description = "Returns paginated tenant users with optional search and sorting")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users listed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid query parameters"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<PageableResponse<UserResponse>>> list(
                        @RequestParam(defaultValue = "0") @Min(0) int page,
                        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
                        @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String search,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        // Parse and validate sort
                        String effectiveSort = SortParseUtil.getEffectiveSortCode(
                                        sort,
                                        SortableFieldsConfig.USER_DEFAULT_SORT);
                        Sort sortObj = SortParseUtil.parse(
                                        effectiveSort,
                                        SortableFieldsConfig.USER_ALLOWED_FIELDS,
                                        SortableFieldsConfig.USER_DEFAULT_SORT);

                        Pageable pageable = PageRequest.of(page, size, sortObj);

                        // Search with pagination
                        Page<User> userPage = userService.searchUsers(search, pageable);

                        // Map to DTOs
                        List<UserResponse> userResponses = userPage.getContent().stream()
                                        .map(UserResponse::from)
                                        .toList();

                        // Build sort config
                        SortConfig sortConfig = SortConfig.of(
                                        effectiveSort,
                                        SortableFieldsConfig.USER_SORT_OPTIONS);

                        // Build pageable response
                        PageableResponse<UserResponse> response = PageableResponse.fromMapped(
                                        userPage,
                                        userResponses,
                                        sortConfig);

                        return ResponseEntity.ok(ApiResponse.success(response));
                } catch (IllegalArgumentException ex) {
                        String message = messageSource.getMessage(
                                        "user.sort.invalid",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error listing users", ex);
                        String message = messageSource.getMessage(
                                        "user.list.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== Get User By ID ==========

        @GetMapping("/{id}")
        @Operation(summary = "Get user by ID", description = "Returns a tenant user by identifier")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<UserResponse>> getUserById(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        validateUserAccess(id);
                        Optional<User> user = userService.getUserById(id);
                        if (user.isPresent()) {
                                return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user.get())));
                        } else {
                                String message = messageSource.getMessage("user.not.found", new Object[] { id },
                                                Locale.forLanguageTag(languageCode));
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body(ApiResponse.error(message));
                        }
                } catch (Exception ex) {
                        log.error("Error getting user", ex);
                        String message = messageSource.getMessage("user.get.error", new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== Get User By Email ==========

        @GetMapping("/email/{email}")
        @Operation(summary = "Get user by email", description = "Returns a tenant user by email address")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid email"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(
                        @PathVariable @Valid @NotBlank @Email String email,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        String sanitizedEmail = sanitizeEmail(email);
                        if (sanitizedEmail == null || sanitizedEmail.trim().isEmpty()) {
                                throw new IllegalArgumentException("Invalid email");
                        }

                        Optional<User> user = userService.findByEmail(sanitizedEmail);
                        if (user.isPresent()) {
                                return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user.get())));
                        } else {
                                String message = messageSource.getMessage("user.email.not.found",
                                                new Object[] { email },
                                                Locale.forLanguageTag(languageCode));
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body(ApiResponse.error(message));
                        }
                } catch (Exception ex) {
                        log.error("Error getting user by email", ex);
                        String message = messageSource.getMessage("user.get.error", new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== Get Current User ==========

        @GetMapping("/current")
        @Operation(summary = "Get current user", description = "Returns the authenticated user profile")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current user returned"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        User currentUser = userService.getCurrentUser();
                        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(currentUser)));
                } catch (Exception ex) {
                        log.error("Error getting current user", ex);
                        String message = messageSource.getMessage("user.current.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== Create User ==========

        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PostMapping
        @Operation(summary = "Create user", description = "Creates a new tenant user")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<UserResponse>> createUser(
                        @Valid @RequestBody CreateUserRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode,
                        HttpServletRequest httpRequest) {
                try {
                        Language language = RequestUtils.parseLanguage(languageCode);
                        String ipAddress = RequestUtils.getClientIpAddress(httpRequest);
                        String userAgent = RequestUtils.getUserAgent(httpRequest);

                        CreateUserInput input = new CreateUserInput(
                                        request.email(),
                                        request.role(),
                                        request.firstName(),
                                        request.lastName(),
                                        request.phone(),
                                        request.jobTitle(),
                                        request.department(),
                                        request.isActive(),
                                        request.notes(),
                                        ipAddress,
                                        userAgent,
                                        language);
                        SecuritySettingsResult securitySettings = securitySettingsService.getSecuritySettings();
                        User createdUser = userService.createUser(input);
                        String messageKey = "user.create.success";
                        if (securitySettings.policy() == TwoFactorPolicy.REQUIRED) {
                                messageKey = "user.create.2fa.info";
                        }
                        String message = messageSource.getMessage(messageKey, null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success(message, UserResponse.from(createdUser)));
                } catch (IllegalArgumentException ex) {
                        log.error("Error creating user - validation failed", ex);
                        String message = messageSource.getMessage("user.create.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error creating user", ex);
                        String message = messageSource.getMessage("user.create.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== Update User ==========

        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PutMapping("/{id}")
        @Operation(summary = "Update user", description = "Updates an existing tenant user")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<UserResponse>> updateUser(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @Valid @RequestBody UpdateUserRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        validateUserAccess(id);
                        // Convert Presentation DTO to Application Input
                        UpdateUserInput input = new UpdateUserInput(
                                        request.email(),
                                        request.role(),
                                        request.firstName(),
                                        request.lastName(),
                                        request.phone(),
                                        request.jobTitle(),
                                        request.department(),
                                        request.isActive(),
                                        request.notes());
                        User updatedUser = userService.updateUser(id, input);
                        String message = messageSource.getMessage("user.update.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, UserResponse.from(updatedUser)));
                } catch (UserNotFoundException ex) {
                        String message = messageSource.getMessage("user.not.found", new Object[] { id },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.error(message));
                } catch (IllegalArgumentException ex) {
                        String message = messageSource.getMessage("user.update.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error updating user", ex);
                        String message = messageSource.getMessage("user.update.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== Delete User ==========

        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @DeleteMapping("/{id}")
        @Operation(summary = "Delete user", description = "Deletes a tenant user")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Delete failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<Void>> deleteUser(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        validateUserAccess(id);
                        userService.deleteUser(id);
                        String message = messageSource.getMessage("user.delete.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, null));
                } catch (Exception ex) {
                        log.error("Error deleting user", ex);
                        String message = messageSource.getMessage("user.delete.error", new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== Activate User ==========

        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PostMapping("/{id}/activate")
        @Operation(summary = "Activate user", description = "Activates a tenant user account")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User activated"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Activation failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<UserResponse>> activateUser(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        User activatedUser = userService.activateUser(id);
                        String message = messageSource.getMessage("user.activated.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, UserResponse.from(activatedUser)));
                } catch (Exception ex) {
                        log.error("Error activating user", ex);
                        String message = messageSource.getMessage("user.activate.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== Passwords ==========

        /**
         * Send password reset email to user.
         * 
         * <p>
         * Sends a secure password reset link to the user's email address.
         * The user will set their own password through the link.
         * 
         * @param id           User ID to send password reset email
         * @param languageCode Language code for email content
         * @param httpRequest  HTTP request for extracting IP and user agent
         * @return Success response with confirmation message
         */
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PostMapping("/{id}/reset-password")
        @Operation(summary = "Send reset password email", description = "Sends password reset email to selected user")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reset email sent"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<Void>> resetPassword(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode,
                        HttpServletRequest httpRequest) {
                try {
                        validateUserAccess(id);

                        User user = userService.getUserById(id)
                                        .orElseThrow(() -> new UserNotFoundException(id));

                        Language language = RequestUtils.parseLanguage(languageCode);
                        String ipAddress = RequestUtils.getClientIpAddress(httpRequest);
                        String userAgent = RequestUtils.getUserAgent(httpRequest);

                        Long tenantId = Long.valueOf(tenantContext.getTenantId());
                        String subdomain = tenantContext.getSubdomain();

                        authenticationService.requestPasswordReset(
                                        user.getEmail(),
                                        tenantId,
                                        subdomain,
                                        ipAddress,
                                        userAgent,
                                        language);

                        String message = messageSource.getMessage(
                                        "user.password.reset.email.sent",
                                        new Object[] { user.getEmail() },
                                        Locale.forLanguageTag(languageCode));

                        log.info("Password reset email sent for user ID: {}", id);
                        return ResponseEntity.ok(ApiResponse.success(message, null));

                } catch (UserNotFoundException ex) {
                        String message = messageSource.getMessage("user.not.found",
                                        new Object[] { id }, Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error sending password reset email for user ID: {}", id, ex);
                        String message = messageSource.getMessage("user.password.reset.email.error",
                                        null, Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== Deactivate User ==========

        @PreAuthorize("hasRole('TENANT_ADMIN')")
        @PostMapping("/{id}/deactivate")
        @Operation(summary = "Deactivate user", description = "Deactivates a tenant user account")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deactivated"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Deactivation failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        User deactivatedUser = userService.deactivateUser(id);
                        String message = messageSource.getMessage("user.deactivated.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, UserResponse.from(deactivatedUser)));
                } catch (Exception ex) {
                        log.error("Error deactivating user", ex);
                        String message = messageSource.getMessage("user.deactivate.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== Helper Methods ==========

        private void validateUserAccess(Long userId) {
                if (userId == null || userId <= 0) {
                        throw new IllegalArgumentException("Invalid user ID");
                }
        }

        private String sanitizeEmail(String email) {
                if (email == null) {
                        return null;
                }
                return email.trim().toLowerCase();
        }
}
