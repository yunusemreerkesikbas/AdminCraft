package com.backend.application.dto.request;

import com.backend.domain.enums.NavigationItemType;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateEntryRequest(
        @Size(max = 100, message = "UID must be at most 100 characters") String uid,

        NavigationItemType itemType,

        @Size(max = 100, message = "Item ID must be at most 100 characters") String itemId,

        @Size(max = 500, message = "URL must be at most 500 characters") String url,

        @Size(max = 200, message = "Link name must be at most 200 characters") String linkName,

        @Size(max = 10, message = "Link color must be at most 10 characters") @Pattern(regexp = "^(#[0-9A-Fa-f]{6})?$", message = "Link color must be in HEX format (#RRGGBB)") String linkColor,

        @Size(max = 20, message = "Target must be at most 20 characters") String target,

        Boolean isExternal,

        Boolean isVisible) {
}
