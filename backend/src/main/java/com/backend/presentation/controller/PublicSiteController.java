package com.backend.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.SiteTechnicalService;
import com.backend.shared.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/sites")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public Site", description = "Public site utility endpoints")
public class PublicSiteController {

  private final SiteTechnicalService siteTechnicalService;

  /**
   * Get robots.txt content for the site.
   * Public endpoint for search engine crawlers.
   * Wrapped in ApiResponse as per requirements, although unusual for robots.txt.
   */
  @GetMapping("/robots.txt")
  @Operation(summary = "Get robots.txt", description = "Returns robots.txt content for crawlers")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "robots.txt returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
  })
  public ResponseEntity<ApiResponse<String>> getRobotsTxt() {
    try {
      String robotsTxt = siteTechnicalService.getRobotsTxt();
      return ResponseEntity.ok(ApiResponse.success(robotsTxt));
    } catch (Exception ex) {
      log.error("Error getting robots.txt", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to generate robots.txt: " + ex.getMessage()));
    }
  }
}
