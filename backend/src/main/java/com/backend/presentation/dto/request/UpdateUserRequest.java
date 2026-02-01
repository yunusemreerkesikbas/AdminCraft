package com.backend.presentation.dto.request;

import com.backend.domain.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Email(message = "validation.email.invalid") String email,

        @Size(max = 100, message = "validation.full.name.size") String fullName,

        UserRole role,

        @Size(max = 50, message = "validation.first.name.size") String firstName,

        @Size(max = 50, message = "validation.last.name.size") String lastName,

        @Size(max = 20, message = "validation.phone.size") String phone,

        @Size(max = 100, message = "validation.job.title.size") String jobTitle,

        @Size(max = 100, message = "validation.department.size") String department,

        Boolean isActive,

        @Size(max = 500, message = "validation.user.notes.size") String notes) {
    public UpdateUserRequest {
        email = email != null ? email.trim() : null;
        fullName = fullName != null ? fullName.trim() : null;
        firstName = firstName != null ? firstName.trim() : null;
        lastName = lastName != null ? lastName.trim() : null;
        phone = phone != null ? phone.trim() : null;
        jobTitle = jobTitle != null ? jobTitle.trim() : null;
        department = department != null ? department.trim() : null;
        notes = notes != null ? notes.trim() : null;
    }
}
