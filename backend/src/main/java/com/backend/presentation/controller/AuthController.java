package com.backend.presentation.controller;

import com.backend.application.service.AuthenticationService;
import com.backend.presentation.dto.request.LoginRequest;
import com.backend.presentation.dto.response.LoginResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("Login attempt for email: {}", loginRequest.email());
            
            LoginResponse loginResponse = authenticationService.authenticate(loginRequest);
            
            ApiResponse<LoginResponse> response = new ApiResponse<>(
                "SUCCESS", 
                "Login successful", 
                loginResponse
            );
            
            log.info("Login successful for email: {}", loginRequest.email());
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception ex) {
            log.error("Login failed for email: {} - Error: {}", loginRequest.email(), ex.getMessage());
            ApiResponse<LoginResponse> errorResponse = new ApiResponse<>("ERROR", ex.getMessage(), null);
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        try {
            log.info("Token refresh attempt");
            
            // Remove "Bearer " prefix if present
            String token = refreshToken.startsWith("Bearer ") ? 
                refreshToken.substring(7) : refreshToken;
            
            LoginResponse loginResponse = authenticationService.refreshToken(token);
            
            ApiResponse<LoginResponse> response = new ApiResponse<>(
                "SUCCESS", 
                "Token refreshed successfully", 
                loginResponse
            );
            
            log.info("Token refresh successful");
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception ex) {
            log.error("Token refresh failed - Error: {}", ex.getMessage());
            ApiResponse<LoginResponse> errorResponse = new ApiResponse<>("ERROR", ex.getMessage(), null);
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
    }
}