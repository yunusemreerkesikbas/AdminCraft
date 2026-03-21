package com.backend.infrastructure.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.backend.application.config.StorageConfigProperties;
import com.backend.application.service.StorageAdapter;
import com.backend.domain.enums.StorageProvider;
import com.backend.infrastructure.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@Primary
@ConditionalOnProperty(name = "craftive.storage.provider", havingValue = "s3")
@RequiredArgsConstructor
@Slf4j
public class S3StorageAdapter implements StorageAdapter {

    private final S3Client s3Client;
    private final StorageConfigProperties properties;
    private final TenantContext tenantContext;

    @Override
    public String store(InputStream inputStream, String fileName, String subPath) {
        try {
            byte[] content = inputStream.readAllBytes();
            return store(content, fileName, subPath);
        } catch (IOException e) {
            throw new StorageException("Failed to read input stream for: " + fileName, e);
        }
    }

    @Override
    public String store(byte[] content, String fileName, String subPath) {
        String objectKey = buildObjectKey(fileName, subPath);
        String contentType = detectContentType(fileName);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getS3().getBucket())
                .key(objectKey)
                .contentType(contentType)
                .cacheControl("public, max-age=31536000, immutable")
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(content));
        log.debug("Stored S3 object: {}", objectKey);
        return objectKey;
    }

    @Override
    public byte[] retrieve(String objectKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(properties.getS3().getBucket())
                .key(objectKey)
                .build();
        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    @Override
    public InputStream retrieveAsStream(String objectKey) {
        return new ByteArrayInputStream(retrieve(objectKey));
    }

    @Override
    public boolean delete(String objectKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.getS3().getBucket())
                    .key(objectKey)
                    .build());
            log.debug("Deleted S3 object: {}", objectKey);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete S3 object: {}", objectKey, e);
            return false;
        }
    }

    @Override
    public boolean exists(String objectKey) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(properties.getS3().getBucket())
                    .key(objectKey)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public long getSize(String objectKey) {
        HeadObjectResponse response = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(properties.getS3().getBucket())
                .key(objectKey)
                .build());
        return response.contentLength();
    }

    @Override
    public String getPublicUrl(String objectKey) {
        String base = properties.getS3().getCdnBaseUrl();
        if (base == null || base.isBlank()) {
            throw new IllegalStateException("craftive.storage.s3.cdn-base-url must be set when storage provider is s3");
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + objectKey;
    }

    @Override
    public StorageProvider getProviderType() {
        return StorageProvider.S3;
    }

    // Object key: {subdomain}/{subPath}/{fileName}
    private String buildObjectKey(String fileName, String subPath) {
        String subdomain = tenantContext.getSubdomain();
        if (subdomain == null || subdomain.isBlank()) {
            throw new IllegalStateException("Tenant subdomain must be resolved for S3 storage operations");
        }
        String sanitizedSubPath = (subPath != null) ? subPath.replaceAll("\\.\\.", "").strip() : "";
        if (sanitizedSubPath.isEmpty()) {
            return subdomain + "/" + fileName;
        }
        return subdomain + "/" + sanitizedSubPath + "/" + fileName;
    }

    private String detectContentType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "application/octet-stream";
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            case "mp4" -> "video/mp4";
            case "mp3" -> "audio/mpeg";
            default -> "application/octet-stream";
        };
    }
}
