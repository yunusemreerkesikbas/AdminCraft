package com.backend.application.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record ReorderRequest<T>(
        @NotEmpty(message = "Items list cannot be empty") List<T> items) {
}
