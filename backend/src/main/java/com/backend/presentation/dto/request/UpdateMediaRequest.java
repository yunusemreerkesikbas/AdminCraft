package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateMediaRequest(
    @Size(max = 500, message = "Alt text in Turkish must not exceed 500 characters")
    String altTextTr,
    
    @Size(max = 500, message = "Alt text in English must not exceed 500 characters")
    String altTextEn,
    
    @Size(max = 1000, message = "Description in Turkish must not exceed 1000 characters")
    String descriptionTr,
    
    @Size(max = 1000, message = "Description in English must not exceed 1000 characters")
    String descriptionEn,
    
    @Size(max = 255, message = "Title in Turkish must not exceed 255 characters")
    String titleTr,
    
    @Size(max = 255, message = "Title in English must not exceed 255 characters")
    String titleEn,
    
    String folder,
    String category,
    String tags,
    Boolean isPublic
) {}