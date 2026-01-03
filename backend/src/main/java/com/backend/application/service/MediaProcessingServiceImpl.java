package com.backend.application.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.application.config.StorageConfigProperties;
import com.backend.application.dto.ImageDimensions;
import com.backend.application.dto.ProcessedFormat;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.MediaFormat;
import com.backend.domain.enums.CropMode;
import com.backend.domain.enums.MediaStatus;
import com.backend.domain.enums.StorageProvider;
import com.backend.domain.repository.MediaFormatRepository;
import com.backend.domain.repository.MediaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaProcessingServiceImpl implements MediaProcessingService {

    private final MediaRepository mediaRepository;
    private final MediaFormatRepository formatRepository;
    private final MediaStorageService storageService;
    private final MediaContainerService containerService;
    private final StorageConfigProperties properties;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Async("taskExecutor")
    public CompletableFuture<List<ProcessedFormat>> generateFormats(Long mediaId) {
        MDC.put("mediaId", String.valueOf(mediaId));
        try {
            Media media = mediaRepository.findById(mediaId)
                    .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));

            if (!isProcessingSupported(media.getMimeType())) {
                log.debug("Processing not supported for MIME type: {}", media.getMimeType());
                media.setStatus(MediaStatus.ACTIVE);
                mediaRepository.save(media);
                return CompletableFuture.completedFuture(Collections.emptyList());
            }

            media.setStatus(MediaStatus.PROCESSING);
            mediaRepository.save(media);

            List<MediaFormat> formats = formatRepository.findByCodeIn(
                    properties.getProcessing().getAutoGenerateFormats());

            byte[] masterData = storageService.retrieve(media.getFilePath());

            ImageDimensions dimensions = extractDimensions(masterData);
            if (dimensions != null && (media.getWidth() == null || media.getHeight() == null)) {
                media.setWidth(dimensions.width());
                media.setHeight(dimensions.height());
            }

            List<ProcessedFormat> results = new ArrayList<>();
            for (MediaFormat format : formats) {
                try {
                    ProcessedFormat result = processFormat(media, format, masterData);
                    results.add(result);
                } catch (Exception e) {
                    log.error("Failed to generate format {} for media {}: {}",
                            format.getCode(), mediaId, e.getMessage());
                }
            }

            media.setStatus(MediaStatus.ACTIVE);
            mediaRepository.save(media);

            log.info("Generated {} formats for media {}", results.size(), mediaId);
            return CompletableFuture.completedFuture(results);

        } catch (Exception e) {
            log.error("Format generation failed for media {}: {}", mediaId, e.getMessage());
            mediaRepository.findById(mediaId).ifPresent(m -> {
                m.setStatus(MediaStatus.FAILED);
                mediaRepository.save(m);
            });
            return CompletableFuture.failedFuture(e);
        } finally {
            MDC.clear();
        }
    }

    @Override
    public ProcessedFormat generateFormat(Long mediaId, String formatCode) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));

        MediaFormat format = formatRepository.findByCode(formatCode)
                .orElseThrow(() -> new IllegalArgumentException("Format not found: " + formatCode));

        byte[] masterData = storageService.retrieve(media.getFilePath());
        return processFormat(media, format, masterData);
    }

    @Override
    public ImageDimensions extractDimensions(byte[] imageData) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image != null) {
                return new ImageDimensions(image.getWidth(), image.getHeight());
            }
        } catch (IOException e) {
            log.warn("Failed to extract dimensions: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean isProcessingSupported(String mimeType) {
        return mimeType != null &&
                properties.getProcessing().getSupportedImageTypes().contains(mimeType.toLowerCase());
    }

    private ProcessedFormat processFormat(Media master, MediaFormat format, byte[] masterData) {
        byte[] resizedData = resizeImage(masterData, format);
        String variantFileName = generateVariantFileName(master, format);

        MediaStorageService.StoredFileResult stored = storageService.storeProcessedImage(
                resizedData, variantFileName, "variants");

        return transactionTemplate.execute(status -> {
            Media variant = createVariantMedia(master, format, stored, resizedData.length);
            variant = mediaRepository.save(variant);

            containerService.addVariant(master.getId(), format.getId(), variant.getId());

            return new ProcessedFormat(format.getCode(), variant.getId(), stored.filePath());
        });
    }

    private byte[] resizeImage(byte[] imageData, MediaFormat format) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            var builder = Thumbnails.of(new ByteArrayInputStream(imageData))
                    .size(format.getWidth(), format.getHeight())
                    .outputQuality(format.getQuality() / 100.0);

            if (format.getCropMode() == CropMode.COVER || format.getCropMode() == CropMode.FILL) {
                builder.crop(Positions.CENTER);
            }

            builder.toOutputStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to resize image", e);
        }
    }

    private String generateVariantFileName(Media master, MediaFormat format) {
        String baseName = master.getFileName();
        int dotIndex = baseName.lastIndexOf('.');
        String nameWithoutExt = dotIndex > 0 ? baseName.substring(0, dotIndex) : baseName;
        String ext = dotIndex > 0 ? baseName.substring(dotIndex) : ".jpg";
        return nameWithoutExt + "_" + format.getCode() + ext;
    }

    private Media createVariantMedia(Media master, MediaFormat format,
            MediaStorageService.StoredFileResult stored, int fileSize) {
        Media variant = new Media();
        variant.setOriginalName(master.getOriginalName() + "_" + format.getCode());
        variant.setFileName(stored.fileName());
        variant.setFilePath(stored.filePath());
        variant.setMimeType(stored.mimeType());
        variant.setFileExtension(stored.extension());
        variant.setFileSize((long) fileSize);
        variant.setWidth(format.getWidth());
        variant.setHeight(format.getHeight());
        variant.setStorageProvider(StorageProvider.valueOf(properties.getProvider().toUpperCase()));
        variant.setStatus(MediaStatus.ACTIVE);
        variant.setIsPublic(master.getIsPublic());
        variant.setUploadedBy(master.getUploadedBy());
        variant.setFolder(master.getFolder());
        variant.setUsageCount(0);
        return variant;
    }
}
