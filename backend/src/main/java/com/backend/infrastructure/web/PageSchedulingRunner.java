package com.backend.infrastructure.web;

import com.backend.application.service.PageService;
import com.backend.domain.enums.PageStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PageSchedulingRunner {

  private final PageService pageService;

  // Runs every minute to publish scheduled pages
  @Scheduled(cron = "0 * * * * *")
  public void processScheduledPages() {
    try {
      int published = pageService.publishDueScheduledPages(LocalDateTime.now());
      if (published > 0) {
        log.info("Scheduled processor published {} pages", published);
      }
    } catch (Exception ex) {
      log.error("Scheduled page processing failed: {}", ex.getMessage());
    }
  }
}
