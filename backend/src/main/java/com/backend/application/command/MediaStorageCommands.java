package com.backend.application.command;

import org.springframework.web.multipart.MultipartFile;

public final class MediaStorageCommands {

    private MediaStorageCommands() {}

    public record StoreFileCommand(
        MultipartFile file,
        String folder,
        Long uploadedBy
    ) {}

    public record StoreProcessedImageCommand(
        byte[] imageData,
        String fileName,
        String folder
    ) {}

    public record DeleteFileCommand(
        String filePath
    ) {}
}
