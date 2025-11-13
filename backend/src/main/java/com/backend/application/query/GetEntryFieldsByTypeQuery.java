package com.backend.application.query;

public record GetEntryFieldsByTypeQuery(Long componentTypeId) {
    public GetEntryFieldsByTypeQuery {
        if (componentTypeId == null) {
            throw new IllegalArgumentException("Component type ID is required");
        }
    }
}

