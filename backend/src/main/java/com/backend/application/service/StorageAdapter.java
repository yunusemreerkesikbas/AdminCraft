package com.backend.application.service;

import java.io.InputStream;

import com.backend.domain.enums.StorageProvider;

public interface StorageAdapter {

    String store(InputStream inputStream, String fileName, String subPath);

    String store(byte[] content, String fileName, String subPath);

    byte[] retrieve(String filePath);

    InputStream retrieveAsStream(String filePath);

    boolean delete(String filePath);

    boolean exists(String filePath);

    long getSize(String filePath);

    String getPublicUrl(String filePath);

    StorageProvider getProviderType();
}
