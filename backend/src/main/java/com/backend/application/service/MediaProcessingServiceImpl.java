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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.application.config.StorageConfigProperties;
import com.backend.application.dto.ImageDimensions;
import com.backend.application.dto.ProcessedFormat;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.MediaFormat;
import com.backend.domain.enums.CropMode;
import com.backend.domain.enums.MediaStatus;
import com.backend.domain.enums.OutputFormat;
import com.backend.domain.enums.StorageProvider;
import com.backend.domain.repository.MediaFormatRepository;
import com.backend.domain.repository.MediaRepository;
import com.backend.presentation.dto.request.GenerateFormatRequest;

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
          ProcessedFormat result = processFormat(media, format, masterData, format.getOutputFormat());
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
    return processFormat(media, format, masterData, format.getOutputFormat());
  }

  @Override
  @Transactional
  public Media generateSingleFormat(Long mediaId, GenerateFormatRequest request) {
    log.debug("Generating format for media {}: {}", mediaId, request);

    Media master = mediaRepository.findById(mediaId)
        .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));

    if (!isProcessingSupported(master.getMimeType())) {
      throw new IllegalArgumentException("Processing not supported for this media type");
    }

    // Determine format configuration
    MediaFormat format;
    OutputFormat outputFormat = request.getEffectiveOutputFormat();

    if (request.isPreset()) {
      // Use preset format
      format = formatRepository.findByCode(request.formatCode())
          .orElseThrow(() -> new IllegalArgumentException("Format not found: " + request.formatCode()));

      // Override output format if specified in request
      if (request.outputFormat() != null) {
        outputFormat = request.outputFormat();
      } else if (format.getOutputFormat() != null) {
        outputFormat = format.getOutputFormat();
      }
    } else if (request.isCustom()) {
      // Create ephemeral format for custom dimensions
      format = new MediaFormat();
      format.setCode("CUSTOM_" + request.customWidth() + "x" + request.customHeight());
      format.setName("Custom " + request.customWidth() + "x" + request.customHeight());
      format.setWidth(request.customWidth());
      format.setHeight(request.customHeight());
      format.setQuality(request.getEffectiveQuality());
      format.setCropMode(request.getEffectiveCropMode());
      format.setOutputFormat(outputFormat);
    } else {
      throw new IllegalArgumentException(
          "Either formatCode or custom dimensions (width/height) must be provided");
    }

    byte[] masterData = storageService.retrieve(master.getFilePath());

    Media variant = createAndStoreVariant(master, format, outputFormat, masterData);

    log.info("Generated variant {} for media {}: {}x{} {}",
        variant.getUid(), master.getUid(), format.getWidth(), format.getHeight(), variant.getMimeType());

    return variant;
  }

  @Override
  @Transactional
  public void deleteVariant(Long mediaId, Long variantId) {
    log.debug("Deleting variant {} from media {}", variantId, mediaId);

    Media master = mediaRepository.findById(mediaId)
        .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));

    Media variant = mediaRepository.findById(variantId)
        .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + variantId));

    // Prevent deleting the master/original
    if (master.getId().equals(variantId)) {
      throw new IllegalArgumentException("Cannot delete the original media");
    }

    String filePath = variant.getFilePath();

    // Remove from container
    containerService.removeVariant(mediaId, variantId);

    // Delete variant entity
    mediaRepository.deleteById(variantId);

    // Delete physical file
    try {
      storageService.delete(filePath);
    } catch (Exception e) {
      log.warn("Failed to delete variant file {}: {}", filePath, e.getMessage());
    }

    log.info("Deleted variant {} from media {}", variantId, mediaId);
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

  private ProcessedFormat processFormat(Media master, MediaFormat format, byte[] masterData,
      OutputFormat outputFormat) {

    return transactionTemplate.execute(status -> {
      Media variant = createAndStoreVariant(master, format, outputFormat, masterData);
      return new ProcessedFormat(format.getCode(), variant.getId(), variant.getFilePath());
    });
  }

  private Media createAndStoreVariant(Media master, MediaFormat format, OutputFormat outputFormat,
      byte[] masterData) {
    byte[] resizedData = resizeImageWithFormat(masterData, format, outputFormat);
    String extension = determineFileExtension(outputFormat, master.getFileExtension());
    String variantFileName = generateVariantFileNameWithExtension(master, format, extension);
    String mimeType = determineMimeType(outputFormat, master.getMimeType());

    MediaStorageService.StoredFileResult stored = storageService.storeProcessedImage(
        resizedData, variantFileName, "variants");

    Media variant = createVariantMediaWithDetails(master, format, stored, resizedData.length, mimeType);
    variant = mediaRepository.save(variant);

    // Only add to container if it's a persisted format (has ID)
    if (format.getId() != null) {
      containerService.addVariant(master.getId(), format.getId(), variant.getId());
    }

    return variant;
  }

  private byte[] resizeImageWithFormat(byte[] imageData, MediaFormat format, OutputFormat outputFormat) {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      var builder = Thumbnails.of(new ByteArrayInputStream(imageData))
          .size(format.getWidth(), format.getHeight())
          .outputQuality(format.getQuality() / 100.0);

      if (format.getCropMode() == CropMode.COVER || format.getCropMode() == CropMode.FILL) {
        builder.crop(Positions.CENTER);
      }

      // Set output format if not ORIGINAL
      String formatType = getOutputFormatType(outputFormat);
      if (formatType != null) {
        builder.outputFormat(formatType);
      }

      builder.toOutputStream(outputStream);
      return outputStream.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to resize image", e);
    }
  }

  private String getOutputFormatType(OutputFormat outputFormat) {
    if (outputFormat == null || outputFormat == OutputFormat.ORIGINAL) {
      return null; // Keep original format
    }
    return switch (outputFormat) {
      case JPEG -> "jpg";
      case PNG -> "png";
      case WEBP -> "webp";
      default -> null;
    };
  }

  private String determineFileExtension(OutputFormat outputFormat, String originalExtension) {
    if (outputFormat == null || outputFormat == OutputFormat.ORIGINAL) {
      return originalExtension != null ? originalExtension : "jpg";
    }
    return switch (outputFormat) {
      case JPEG -> "jpg";
      case PNG -> "png";
      case WEBP -> "webp";
      default -> originalExtension != null ? originalExtension : "jpg";
    };
  }

  private String determineMimeType(OutputFormat outputFormat, String originalMimeType) {
    if (outputFormat == null || outputFormat == OutputFormat.ORIGINAL) {
      return originalMimeType;
    }
    return switch (outputFormat) {
      case JPEG -> "image/jpeg";
      case PNG -> "image/png";
      case WEBP -> "image/webp";
      default -> originalMimeType;
    };
  }

  private String generateVariantFileNameWithExtension(Media master, MediaFormat format, String extension) {
    String baseName = master.getFileName();
    int dotIndex = baseName.lastIndexOf('.');
    String nameWithoutExt = dotIndex > 0 ? baseName.substring(0, dotIndex) : baseName;
    return nameWithoutExt + "_" + format.getCode() + "." + extension;
  }

  private Media createVariantMediaWithDetails(Media master, MediaFormat format,
      MediaStorageService.StoredFileResult stored, int fileSize, String mimeType) {
    Media variant = new Media();
    variant.setOriginalName(master.getOriginalName() + "_" + format.getCode());
    variant.setFileName(stored.fileName());
    variant.setFilePath(stored.filePath());
    variant.setMimeType(mimeType);
    variant.setFileExtension(determineFileExtension(format.getOutputFormat(), stored.extension()));
    variant.setFileSize((long) fileSize);
    variant.setWidth(format.getWidth());
    variant.setHeight(format.getHeight());
    variant.setStorageProvider(StorageProvider.valueOf(properties.getProvider().toUpperCase()));
    variant.setStatus(MediaStatus.ACTIVE);
    variant.setIsPublic(master.getIsPublic());
    variant.setUploadedBy(master.getUploadedBy());
    variant.setUsageCount(0);
    return variant;
  }
}
