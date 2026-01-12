package com.backend.domain.util;

import java.security.SecureRandom;
import java.util.UUID;

public final class UuidUidGenerator {

    private static final String DEFAULT_PREFIX = "cmsitem";
    private static final int UID_RANDOM_DIGITS = 8;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private UuidUidGenerator() {
        throw new AssertionError("UuidUidGenerator is a utility class and should not be instantiated");
    }

    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    public static String generateUid() {
        return generateUid(null);
    }

    public static String generateUid(String prefix) {
        String effectivePrefix = (prefix == null || prefix.trim().isEmpty())
                ? DEFAULT_PREFIX
                : prefix.trim();

        // Generate 8 random digits using SecureRandom
        StringBuilder randomDigits = new StringBuilder(UID_RANDOM_DIGITS);
        for (int i = 0; i < UID_RANDOM_DIGITS; i++) {
            randomDigits.append(SECURE_RANDOM.nextInt(10));
        }

        return effectivePrefix + "_" + randomDigits.toString();
    }

    public static boolean isValidUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidUid(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            return false;
        }
        // Pattern: prefix_XXXXXXXX (prefix can be any non-empty string, followed by
        // underscore and 8 digits)
        return uid.matches("^[a-zA-Z0-9]+_\\d{8}$");
    }
}
