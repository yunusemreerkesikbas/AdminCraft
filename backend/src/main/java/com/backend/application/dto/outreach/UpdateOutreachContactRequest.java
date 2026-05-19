package com.backend.application.dto.outreach;

import com.backend.domain.enums.OutreachContactStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOutreachContactRequest(
    @NotBlank @Size(max = 255) String fullName,
    @NotBlank @Email String email,
    @Size(max = 255) String companyName,
    @Size(max = 100) String city,
    @Size(max = 2000) String notes,
    OutreachContactStatus status
) {
}
