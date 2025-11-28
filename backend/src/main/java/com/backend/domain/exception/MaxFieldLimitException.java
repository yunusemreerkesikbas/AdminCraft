package com.backend.domain.exception;

public class MaxFieldLimitException extends RuntimeException {
    public MaxFieldLimitException(String message) {
        super(message);
    }
    
    public MaxFieldLimitException(int maxFields, Long componentTypeId) {
        super(String.format(
            "Component type %d has reached maximum field limit of %d", 
            componentTypeId, maxFields
        ));
    }
}

