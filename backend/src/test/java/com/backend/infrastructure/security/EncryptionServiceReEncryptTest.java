package com.backend.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

@DisplayName("SEC-010: EncryptionService.reEncryptIfLegacy")
class EncryptionServiceReEncryptTest {

    private static final String TEST_KEY = "TestKey32CharForEncryptionTest!!";

    private EncryptionService service;

    @BeforeEach
    void setUp() {
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{"dev"});
        service = new EncryptionService(TEST_KEY, env);
    }

    @Test
    @DisplayName("SEC-010: GCM-encrypted value → Optional.empty() (already migrated, no action)")
    void reEncryptIfLegacy_gcmValue_returnsEmpty() {
        String gcmCiphertext = service.encrypt("my-secret-value");

        Optional<String> result = service.reEncryptIfLegacy(gcmCiphertext);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SEC-010: ECB-encrypted value → new GCM ciphertext, plaintext preserved")
    void reEncryptIfLegacy_ecbValue_returnsNewGcmCiphertext() throws Exception {
        String ecbCiphertext = ecbEncrypt("my-secret-value");

        Optional<String> result = service.reEncryptIfLegacy(ecbCiphertext);

        assertThat(result).isPresent();
        assertThat(service.decrypt(result.get())).isEqualTo("my-secret-value");
    }

    @Test
    @DisplayName("SEC-010: garbage base64 → Optional.empty() (no exception thrown)")
    void reEncryptIfLegacy_garbage_returnsEmpty() {
        String garbage = Base64.getEncoder().encodeToString("not-a-valid-ciphertext".getBytes(StandardCharsets.UTF_8));

        Optional<String> result = service.reEncryptIfLegacy(garbage);

        assertThat(result).isEmpty();
    }

    private String ecbEncrypt(String plain) throws Exception {
        byte[] keyBytes = TEST_KEY.substring(0, 32).getBytes(StandardCharsets.UTF_8);
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
    }
}
