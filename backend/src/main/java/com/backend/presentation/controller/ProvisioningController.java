package com.backend.presentation.controller;

import com.backend.application.service.ProvisioningService;
import com.backend.presentation.dto.response.ProvisioningJobResponse;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/provisioning")
@RequiredArgsConstructor
@Slf4j
@Validated
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
public class ProvisioningController {

  private final ProvisioningService provisioningService;
  private final MessageSource messageSource;

  @GetMapping("/jobs/{jobUuid}")
  public ResponseEntity<ApiResponse<ProvisioningJobResponse>> getJobStatus(
      @PathVariable @NotBlank String jobUuid,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
    try {
      ProvisioningJobResponse response = provisioningService.getJobStatus(jobUuid);
      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (IllegalArgumentException ex) {
      log.warn("Provisioning job not found: {}", jobUuid);
      String message = messageSource.getMessage("provisioning.job.not.found",
          new Object[] { jobUuid }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error(message));
    } catch (Exception ex) {
      log.error("Error getting provisioning job status for uuid={}: {}", jobUuid, ex.getMessage());
      String message = messageSource.getMessage("provisioning.job.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }
}
