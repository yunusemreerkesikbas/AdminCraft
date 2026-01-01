package com.backend.application.command;

public final class MediaProcessingCommands {

    private MediaProcessingCommands() {}

    public record GenerateFormatsCommand(Long mediaId) {}

    public record GenerateSingleFormatCommand(Long mediaId, String formatCode) {}

    public record ProcessedFormat(
        String formatCode,
        Long variantMediaId,
        String filePath
    ) {}

    public record ImageDimensions(
        int width,
        int height
    ) {}
}
