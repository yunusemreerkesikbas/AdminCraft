package com.backend.infrastructure.security;

import com.backend.domain.port.EncryptionServicePort;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Component
public class EncryptionService implements EncryptionServicePort {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;
    private static final String PLACEHOLDER_KEY = "CHANGE_THIS_TO_32_CHAR_KEY_IN_PROD";

    private final SecretKey secretKey;
    private final String rawKey;
    private final Environment environment;

    public EncryptionService(@Value("${app.encryption.secret-key}") String encryptionKey, Environment environment) {
        if (encryptionKey == null || encryptionKey.length() < 32) {
            throw new IllegalArgumentException("Encryption key must be at least 32 characters");
        }
        this.rawKey = encryptionKey;
        this.environment = environment;
        byte[] keyBytes = encryptionKey.substring(0, 32).getBytes(StandardCharsets.UTF_8);
        this.secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    @PostConstruct
    void validateKey() {
        boolean isDev = java.util.Arrays.asList(environment.getActiveProfiles()).contains("dev");
        if (!isDev && PLACEHOLDER_KEY.equals(rawKey)) {
            throw new IllegalStateException(
                    "Encryption key is set to the placeholder value. Set RECAPTCHA_MASTER_KEY environment variable.");
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Failed to encrypt data", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isBlank()) {
            return null;
        }

        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("GCM decryption failed, attempting legacy ECB decryption");
            return decryptLegacy(encryptedText);
        }
    }

    /**
     * SEC-010: If the ciphertext is ECB-encrypted, decrypts it and re-encrypts with GCM.
     * Returns Optional.empty() when the value is already GCM (no action needed).
     */
    @Override
    public java.util.Optional<String> reEncryptIfLegacy(String ciphertext) {
        if (ciphertext == null || ciphertext.isBlank()) {
            return java.util.Optional.empty();
        }
        final byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(ciphertext);
        } catch (IllegalArgumentException e) {
            log.debug("SEC-010: skip re-encrypt, invalid Base64 ciphertext");
            return java.util.Optional.empty();
        }
        try {
            if (decoded.length <= IV_LENGTH) {
                throw new IllegalArgumentException("Too short for GCM");
            }
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);
            byte[] ct = new byte[decoded.length - IV_LENGTH];
            System.arraycopy(decoded, IV_LENGTH, ct, 0, ct.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
            cipher.doFinal(ct);
            // GCM succeeded → already migrated
            return java.util.Optional.empty();
        } catch (Exception e) {
            // GCM failed → attempt ECB → re-encrypt to GCM
            try {
                Cipher ecb = Cipher.getInstance("AES/ECB/PKCS5Padding");
                ecb.init(Cipher.DECRYPT_MODE, secretKey);
                String plain = new String(ecb.doFinal(decoded), StandardCharsets.UTF_8);
                return java.util.Optional.ofNullable(encrypt(plain));
            } catch (Exception ecbEx) {
                log.warn("SEC-010: ciphertext is neither valid GCM nor ECB — skipping", ecbEx);
                return java.util.Optional.empty();
            }
        }
    }

    // TODO: Remove after all stored secrets are confirmed GCM (SEC-010 Phase 2)
    private String decryptLegacy(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to decrypt data with both GCM and legacy ECB", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
