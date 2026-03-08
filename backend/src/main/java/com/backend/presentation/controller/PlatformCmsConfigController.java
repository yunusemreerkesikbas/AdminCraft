package com.backend.presentation.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.RecaptchaService;
import com.backend.shared.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/platform/cms/config")
@RequiredArgsConstructor
public class PlatformCmsConfigController {

    private final RecaptchaService recaptchaService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getPublicConfig() {
        return ResponseEntity.ok(ApiResponse.success(recaptchaService.getPublicClientConfig()));
    }
}
