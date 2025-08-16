package com.backend.presentation.controller;

import com.backend.application.service.UserService;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.shared.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(user.get()));
            } else {
                String message = messageSource.getMessage("user.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting user by id", ex);
            String message = messageSource.getMessage("user.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(
            @PathVariable String email,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<User> user = userService.findByEmail(email);
            if (user.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(user.get()));
            } else {
                String message = messageSource.getMessage("user.email.not.found", new Object[]{email}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting user by email", ex);
            String message = messageSource.getMessage("user.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByTenant(
            @PathVariable Long tenantId,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            List<User> users = userService.getUsersByTenantId(tenantId);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception ex) {
            log.error("Error getting users by tenant", ex);
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
            log.error("Error getting all users", ex);
            String message = messageSource.getMessage("user.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            userService.deleteUser(id);
            String message = messageSource.getMessage("user.delete.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Error deleting user", ex);
            String message = messageSource.getMessage("user.delete.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }
}