package com.backend.infrastructure.config;

import java.net.URI;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.backend.application.config.StorageConfigProperties;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "admincraft.storage.provider", havingValue = "s3")
@RequiredArgsConstructor
public class S3Config {

    private final StorageConfigProperties properties;

    @Bean
    public S3Client s3Client() {
        StorageConfigProperties.S3 s3 = properties.getS3();
        if (s3.getAccessKey() == null || s3.getAccessKey().isBlank()) {
            throw new IllegalStateException("SPACES_ACCESS_KEY must be set when storage provider is s3");
        }
        if (s3.getSecretKey() == null || s3.getSecretKey().isBlank()) {
            throw new IllegalStateException("SPACES_SECRET_KEY must be set when storage provider is s3");
        }
        return S3Client.builder()
                .endpointOverride(URI.create(s3.getEndpoint()))
                .region(Region.of(s3.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())))
                .build();
    }
}
