package com.backend.domain.port;

import java.util.Optional;

/**
 * Domain port for encryption/decryption operations.
 * Allows Application layer to encrypt sensitive data without depending on Infrastructure.
 */
public interface EncryptionServicePort {
    /**
     * Encrypt plaintext using AES-GCM.
     * @param plainText plaintext to encrypt
     * @return encrypted text (Base64 encoded)
     */
    String encrypt(String plainText);
    
    /**
     * Decrypt encrypted text using AES-GCM.
     * @param encryptedText encrypted text (Base64 encoded)
     * @return decrypted plaintext
     */
    String decrypt(String encryptedText);

    /**
     * SEC-010: If ciphertext is legacy ECB, decrypt and re-encrypt with GCM.
     * Returns empty when already GCM or when migration is not applicable.
     */
    Optional<String> reEncryptIfLegacy(String ciphertext);
}
