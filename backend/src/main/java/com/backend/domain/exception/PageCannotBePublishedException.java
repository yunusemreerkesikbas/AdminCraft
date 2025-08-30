package com.backend.domain.exception;

public class PageCannotBePublishedException extends RuntimeException {
    
    public PageCannotBePublishedException(Long pageId) {
        super("Page cannot be published with id: " + pageId);
    }
    
    public PageCannotBePublishedException(String message) {
        super(message);
    }
}