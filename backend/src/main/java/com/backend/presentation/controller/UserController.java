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
import com.backend.application.dto.UpdateUserInput;
import com.backend.application.service.UserService;
import com.backend.domain.entity.User;
import com.backend.domain.exception.UserNotFoundException;
import com.backend.presentation.dto.request.ChangePasswordRequest;
import com.backend.presentation.dto.request.CreateUserRequest;
import com.backend.presentation.dto.request.UpdateUserRequest;
import com.backend.presentation.dto.response.PageableResponse;
import com.backend.presentation.dto.response.ResetPasswordResponse;
import com.backend.presentation.dto.response.SortConfig;
import com.backend.presentation.dto.response.UserResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityHelper;
import com.backend.shared.common.SortParseUtil;
import com.backend.shared.config.SortableFieldsConfig;

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
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class UserController {

        private final UserService userService;
        private final MessageSource messageSource;
        private final SecurityHelper securityHelper;

        @GetMapping
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

        @PostMapping
        public ResponseEntity<ApiResponse<UserResponse>> createUser(
                        @Valid @RequestBody CreateUserRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        // Convert Presentation DTO to Application Input
                        CreateUserInput input = new CreateUserInput(
                                        request.email(),
                                        request.password(),
                                        request.fullName(),
                                        request.role(),
                                        request.firstName(),
                                        request.lastName(),
                                        request.phone(),
                                        request.jobTitle(),
                                        request.department(),
                                        request.isActive(),
                                        request.notes());
                        User createdUser = userService.createUser(input);
                        String message = messageSource.getMessage("user.create.success", null,
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

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<UserResponse>> updateUser(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @Valid @RequestBody UpdateUserRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        validateUserAccess(id);
                        // Convert Presentation DTO to Application Input
                        UpdateUserInput input = new UpdateUserInput(
                                        request.email(),
                                        request.fullName(),
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

        @DeleteMapping("/{id}")
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

        @PostMapping("/{id}/activate")
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
         * Reset user password and generate a new random password.
         * 
         * <p>
         * <b>Security Note:</b> This endpoint returns the plaintext password in the
         * response
         * for administrative convenience (TENANT_ADMIN can immediately share it with
         * the user).
         * This is acceptable in trusted admin contexts but increases exposure risk
         * through
         * browser logs, network proxies, and APM tools.
         * 
         * <p>
         * For production environments with strict security requirements, consider
         * implementing
         * a token-based reset flow where a one-time reset token is sent via email and
         * the user
         * sets their own password through a secure link.
         * 
         * @param id           User ID to reset password for
         * @param languageCode Language code for response messages
         * @return Response containing the newly generated password
         */
        @PostMapping("/{id}/reset-password")
        public ResponseEntity<ApiResponse<ResetPasswordResponse>> resetPassword(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        validateUserAccess(id);
                        String newPassword = userService.resetPassword(id);
                        String message = messageSource.getMessage("user.password.reset.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, new ResetPasswordResponse(newPassword)));
                } catch (Exception ex) {
                        log.error("Error resetting password", ex);
                        String message = messageSource.getMessage("user.password.reset.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                }
        }

        @PostMapping("/{id}/change-password")
        public ResponseEntity<ApiResponse<Void>> changePassword(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @Valid @RequestBody ChangePasswordRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        validateUserAccess(id);
                        userService.changePassword(id, request.currentPassword(), request.newPassword());
                        String message = messageSource.getMessage("user.password.change.success", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, null));
                } catch (IllegalArgumentException ex) {
                        log.error("Error changing password - validation failed", ex);
                        String message = messageSource.getMessage("user.password.change.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Error changing password", ex);
                        String message = messageSource.getMessage("user.password.change.error",
                                        new Object[] { ex.getMessage() },
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        // ========== Deactivate User ==========

        @PostMapping("/{id}/deactivate")
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
