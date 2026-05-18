package com.backend.presentation.dto.request.outreach;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateOutreachContactRequest(
    @NotBlank String fullName,
    @NotBlank @Email String email,
    String companyName,
    String city,
    String notes,
    String status
) {
}
