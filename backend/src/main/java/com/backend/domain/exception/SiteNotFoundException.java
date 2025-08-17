package com.backend.domain.exception;

/**
 * Exception thrown when a site is not found.
 * This is a runtime exception that indicates a site with the specified identifier does not exist.
 */
public class SiteNotFoundException extends RuntimeException {
    
    public SiteNotFoundException(String message) {
        super(message);
    }
    
    public SiteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SiteNotFoundException(Long siteId) {
        super("Site not found with ID: " + siteId);
    }
}