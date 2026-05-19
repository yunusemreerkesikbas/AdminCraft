package com.backend.application.dto.outreach;

import com.backend.infrastructure.persistence.platform.entity.PlatformOutreachContact;
import com.backend.shared.util.ResponseValueFilter;

public record OutreachContactResponse(
    Long id,
    String uuid,
    String fullName,
    String email,
    String companyName,
    String city,
    String notes,
    String status,
    String createdAt
) {
    public static OutreachContactResponse from(PlatformOutreachContact contact) {
        return new OutreachContactResponse(
            contact.getId(),
            contact.getUuid(),
            contact.getFullName(),
            contact.getEmail(),
            ResponseValueFilter.filterEmptyString(contact.getCompanyName()),
            ResponseValueFilter.filterEmptyString(contact.getCity()),
            ResponseValueFilter.filterEmptyString(contact.getNotes()),
            contact.getStatus() != null ? contact.getStatus().name() : null,
            contact.getCreatedAt() != null ? contact.getCreatedAt().toString() : null
        );
    }
}
