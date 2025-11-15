package com.backend.infrastructure.validation.rules;

import com.backend.infrastructure.validation.ValidationContext;
import com.backend.infrastructure.validation.ValidationResult;
import com.backend.infrastructure.validation.ValidationRule;

public class CountLimitRule<T> implements ValidationRule<T> {
    
    private final int maxCount;
    private final String contextKey;
    
    public CountLimitRule(int maxCount, String contextKey) {
        this.maxCount = maxCount;
        this.contextKey = contextKey;
    }
    
    public CountLimitRule(int maxCount) {
        this(maxCount, "existingCount");
    }
    
    @Override
    public ValidationResult validate(T value, ValidationContext context) {
        if (!context.has(contextKey)) {
            return ValidationResult.success();
        }
        
        Long existingCount = context.get(contextKey);
        if (existingCount == null) {
            return ValidationResult.success();
        }
        
        if (existingCount >= maxCount) {
            return ValidationResult.failure("Maximum count limit (" + maxCount + ") reached");
        }
        
        return ValidationResult.success();
    }
    
    @Override
    public String getRuleName() {
        return "CountLimit";
    }
}

