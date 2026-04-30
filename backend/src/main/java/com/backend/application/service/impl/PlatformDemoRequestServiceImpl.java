package com.backend.application.service.impl;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.backend.application.dto.platform.PlatformDemoRequestAdminDto;
import com.backend.application.dto.platform.PlatformDemoRequestSubmitCommand;
import com.backend.application.service.PlatformDemoRequestService;
import com.backend.application.service.PlatformMailMarketingService;
import com.backend.application.service.RecaptchaService;
import com.backend.domain.entity.PlatformDemoRequest;
import com.backend.domain.repository.PlatformDemoRequestRepository;
import com.backend.shared.common.LogSanitizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformDemoRequestServiceImpl implements PlatformDemoRequestService {

    private static final String RECAPTCHA_ACTION = "landing_demo_request";
    private static final int MESSAGE_PREVIEW_MAX_LEN = 120;
    private static final int DEDUPE_WINDOW_MINUTES = 5;

    private final PlatformDemoRequestRepository repository;
    private final RecaptchaService recaptchaService;
    private final PlatformMailMarketingService mailMarketingService;

    @Override
    @Transactional("platformTransactionManager")
    public void submit(PlatformDemoRequestSubmitCommand command) {
        recaptchaService.verifyToken(command.recaptchaToken(), RECAPTCHA_ACTION);

        String email = command.email().trim().toLowerCase(Locale.ROOT);
        String clientIp = truncate(command.clientIp(), 45);
        LocalDateTime dedupeWindow = LocalDateTime.now().minusMinutes(DEDUPE_WINDOW_MINUTES);

        PlatformDemoRequest entity = PlatformDemoRequest.builder()
                .fullName(command.fullName().trim())
                .email(email)
                .phone(command.phone() != null ? truncate(command.phone(), 40) : null)
                .message(command.message().trim())
                .locale(command.locale().trim())
                .source("landing")
                .clientIp(clientIp)
                .userAgent(truncate(command.userAgent(), 500))
                .build();

        Optional<PlatformDemoRequest> inserted = repository.saveIfNotDuplicateWithinWindow(entity, dedupeWindow);
        if (inserted.isEmpty()) {
            log.info("Demo request duplicate suppressed for email={}", LogSanitizer.maskEmail(email));
            return;
        }

        PlatformDemoRequest saved = inserted.get();

        Runnable sendConfirmation = () -> mailMarketingService.sendDemoRequestConfirmation(
                saved.getEmail(), saved.getFullName(), saved.getLocale());
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendConfirmation.run();
                }
            });
            return;
        }
        sendConfirmation.run();
    }

    @Override
    @Transactional(value = "platformTransactionManager", readOnly = true)
    public Page<PlatformDemoRequestAdminDto> getPage(Pageable pageable, String search) {
        String term = search != null && !search.isBlank() ? search.trim() : null;
        return repository.search(term, pageable).map(this::toDto);
    }

    private PlatformDemoRequestAdminDto toDto(PlatformDemoRequest e) {
        String fullMessage = e.getMessage();
        return new PlatformDemoRequestAdminDto(
            e.getId(),
            e.getUid(),
            e.getFullName(),
            e.getEmail(),
            e.getPhone(),
            fullMessage,
            previewMessage(fullMessage),
            e.getLocale(),
            e.getSource(),
            e.getClientIp(),
            e.getUserAgent(),
            e.getCreatedAt()
        );
    }

    private static String previewMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        if (message.length() <= MESSAGE_PREVIEW_MAX_LEN) {
            return message;
        }
        return message.substring(0, MESSAGE_PREVIEW_MAX_LEN) + "…";
    }

    private static String truncate(String value, int maxLen) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen);
    }
}
