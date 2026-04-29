package com.backend.application.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.contact.ContactRequestAdminDto;
import com.backend.application.dto.contact.ContactRequestSubmitCommand;
import com.backend.application.service.ContactRequestService;
import com.backend.application.service.RecaptchaService;
import com.backend.domain.entity.ContactRequest;
import com.backend.domain.repository.ContactRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContactRequestServiceImpl implements ContactRequestService {

    private static final String RECAPTCHA_ACTION = "landing_contact_request";
    private static final int MESSAGE_PREVIEW_MAX_LEN = 120;

    private final ContactRequestRepository repository;
    private final RecaptchaService recaptchaService;

    @Override
    @Transactional
    public void submit(ContactRequestSubmitCommand command) {
        recaptchaService.verifyToken(command.recaptchaToken(), RECAPTCHA_ACTION);
        ContactRequest entity = new ContactRequest();
        entity.setFullName(command.fullName().trim());
        entity.setSubject(command.subject().trim());
        entity.setMessage(command.message().trim());
        entity.setLocale(command.locale().trim());
        entity.setSource("contact_page");
        entity.setClientIp(truncate(command.clientIp(), 45));
        entity.setUserAgent(truncate(command.userAgent(), 500));
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactRequestAdminDto> getPage(Pageable pageable, String search, String locale) {
        String term = search != null && !search.isBlank() ? search.trim() : null;
        String localeFilter = locale != null && !locale.isBlank() ? locale.trim() : null;
        return repository.search(term, localeFilter, pageable).map(this::toDto);
    }

    private ContactRequestAdminDto toDto(ContactRequest entity) {
        String fullMessage = entity.getMessage();
        return new ContactRequestAdminDto(
                entity.getId(),
                entity.getFullName(),
                entity.getSubject(),
                fullMessage,
                previewMessage(fullMessage),
                entity.getLocale(),
                entity.getSource(),
                entity.getClientIp(),
                entity.getUserAgent(),
                entity.getCreatedAt());
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
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLen || trimmed.contains(",")) {
            return null;
        }
        return trimmed;
    }
}
