package com.backend.domain.exception;

public class PageNotFoundException extends RuntimeException {
    
    public PageNotFoundException(Long pageId) {
        super("Page not found with id: " + pageId);
    }
    
    public PageNotFoundException(String slug, String language) {
        super("Page not found with slug: " + slug + " and language: " + language);
    }
    
    public PageNotFoundException(String message) {
        super(message);
    }
}