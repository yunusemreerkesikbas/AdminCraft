package com.backend.infrastructure.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.backend.domain.repository.PlatformVerificationTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled job to clean up expired platform verification tokens.
 * Runs daily at 03:00 AM to prevent database bloat.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformTokenCleanupJob {

    private final PlatformVerificationTokenRepository platformVerificationTokenRepository;

    /**
     * Delete expired platform verification tokens.
     * Cron: Daily at 03:00 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredTokens() {
        try {
            log.info("Starting platform verification token cleanup job");
            int deletedCount = platformVerificationTokenRepository.deleteExpiredTokens();
            log.info("Platform token cleanup completed. Deleted {} expired tokens", deletedCount);
        } catch (Exception e) {
            log.error("Error during platform token cleanup", e);
        }
    }
}
