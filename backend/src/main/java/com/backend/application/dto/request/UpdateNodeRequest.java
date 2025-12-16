package com.backend.application.dto.request;

import com.backend.domain.enums.NodePosition;

import jakarta.validation.constraints.Size;

public record UpdateNodeRequest(
        @Size(max = 100, message = "UID must be at most 100 characters") String uid,

        @Size(max = 200, message = "Title must be at most 200 characters") String title,

        Long parentId,

        NodePosition position,

        Boolean isVisible,

        Boolean isTab) {
}
