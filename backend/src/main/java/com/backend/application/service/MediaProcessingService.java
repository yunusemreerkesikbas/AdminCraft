package com.backend.application.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.backend.application.dto.ImageDimensions;
import com.backend.application.dto.ProcessedFormat;

public interface MediaProcessingService {

    CompletableFuture<List<ProcessedFormat>> generateFormats(Long mediaId);

    ProcessedFormat generateFormat(Long mediaId, String formatCode);

    ImageDimensions extractDimensions(byte[] imageData);

    boolean isProcessingSupported(String mimeType);
}
