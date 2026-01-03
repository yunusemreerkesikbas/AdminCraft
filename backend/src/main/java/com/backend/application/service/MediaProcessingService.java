package com.backend.application.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.backend.application.dto.ImageDimensions;
import com.backend.application.dto.ProcessedFormat;
import com.backend.domain.entity.Media;
import com.backend.presentation.dto.request.GenerateFormatRequest;

public interface MediaProcessingService {

    CompletableFuture<List<ProcessedFormat>> generateFormats(Long mediaId);

    ProcessedFormat generateFormat(Long mediaId, String formatCode);

    /**
     * Generate a single format variant on-demand.
     * Supports both preset formats and custom dimensions.
     *
     * @param mediaId media ID
     * @param request format generation request
     * @return generated variant media
     */
    Media generateSingleFormat(Long mediaId, GenerateFormatRequest request);

    /**
     * Delete a variant from a media container.
     * The master (original) media cannot be deleted via this method.
     *
     * @param mediaId   master media ID
     * @param variantId variant media ID to delete
     */
    void deleteVariant(Long mediaId, Long variantId);

    ImageDimensions extractDimensions(byte[] imageData);

    boolean isProcessingSupported(String mimeType);
}
