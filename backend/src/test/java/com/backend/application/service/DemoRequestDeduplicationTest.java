package com.backend.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.application.dto.platform.PlatformDemoRequestSubmitCommand;
import com.backend.application.service.impl.PlatformDemoRequestServiceImpl;
import com.backend.domain.entity.PlatformDemoRequest;
import com.backend.domain.repository.PlatformDemoRequestRepository;

// SEC-107: verifies demo request deduplication within the 5-minute window.
@ExtendWith(MockitoExtension.class)
class DemoRequestDeduplicationTest {

    @Mock
    private PlatformDemoRequestRepository repository;
    @Mock
    private RecaptchaService recaptchaService;
    @Mock
    private PlatformMailMarketingService mailMarketingService;

    private PlatformDemoRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PlatformDemoRequestServiceImpl(repository, recaptchaService, mailMarketingService);
    }

    @Test
    @DisplayName("Duplicate submission within 5-min window → save and mail suppressed")
    void duplicate_withinWindow_suppressedSilently() {
        when(recaptchaService.verifyToken(anyString(), anyString())).thenReturn(true);
        when(repository.saveIfNotDuplicateWithinWindow(any(PlatformDemoRequest.class),
                argThat(since -> since != null
                        && !since.isAfter(LocalDateTime.now())
                        && !since.isBefore(LocalDateTime.now().minusMinutes(6)))))
                .thenReturn(Optional.empty());

        service.submit(command());

        verify(repository, never()).save(any(PlatformDemoRequest.class));
        verify(mailMarketingService, never()).sendDemoRequestConfirmation(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("First submission → saved and mail triggered")
    void firstSubmission_savedAndMailSent() {
        when(recaptchaService.verifyToken(anyString(), anyString())).thenReturn(true);
        when(repository.saveIfNotDuplicateWithinWindow(any(PlatformDemoRequest.class),
                argThat(since -> since != null
                        && !since.isAfter(LocalDateTime.now())
                        && !since.isBefore(LocalDateTime.now().minusMinutes(6)))))
                .thenAnswer(inv -> Optional.of(inv.getArgument(0)));

        service.submit(command());

        verify(repository, never()).save(any(PlatformDemoRequest.class));
        verify(mailMarketingService).sendDemoRequestConfirmation(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Different IP same email within window → not treated as duplicate")
    void differentIp_sameEmail_notDuplicate() {
        when(recaptchaService.verifyToken(anyString(), anyString())).thenReturn(true);
        when(repository.saveIfNotDuplicateWithinWindow(any(PlatformDemoRequest.class),
                argThat(since -> since != null
                        && !since.isAfter(LocalDateTime.now())
                        && !since.isBefore(LocalDateTime.now().minusMinutes(6)))))
                .thenAnswer(inv -> Optional.of(inv.getArgument(0)));

        service.submit(commandWithIp("1.2.3.5"));

        verify(repository, never()).save(any(PlatformDemoRequest.class));
        verify(mailMarketingService).sendDemoRequestConfirmation(anyString(), anyString(), anyString());
    }

    private PlatformDemoRequestSubmitCommand command() {
        return commandWithIp("1.2.3.4");
    }

    private PlatformDemoRequestSubmitCommand commandWithIp(String ip) {
        return new PlatformDemoRequestSubmitCommand(
                "Test User", "user@example.com", null,
                "Hello, I need a demo", "en", "", ip, "Mozilla/5.0");
    }
}
