package com.backend.presentation.validation;

/**
 * Enum defining different types of URLs for validation
 * Each type has its own whitelist of allowed domains
 */
public enum SecureUrlType {
    /**
     * For canonical base URLs - only allows tenant domains
     */
    CANONICAL,
    
    /**
     * For social media URLs - allows major social platforms
     */
    SOCIAL_MEDIA,
    
    /**
     * For general URLs - configurable whitelist
     */
    GENERAL
}