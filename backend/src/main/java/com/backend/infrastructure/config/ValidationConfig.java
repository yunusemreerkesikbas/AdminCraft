package com.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import jakarta.validation.Validator;

/**
 * Configuration class for validation settings.
 * Enables method-level validation for @Valid annotations on method parameters.
 */
@Configuration
public class ValidationConfig {
    
    /**
     * Bean for method validation support.
     * This enables validation of method parameters annotated with @Valid.
     * 
     * @return MethodValidationPostProcessor instance
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator());
        return processor;
    }
    
    /**
     * Bean for creating a validator instance.
     * 
     * @return Validator instance
     */
    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }
}