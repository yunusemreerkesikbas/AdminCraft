package com.backend.presentation.commerce;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotBlank String variantUid,
        @NotNull @Min(1) @Max(99) Integer quantity) {
}
