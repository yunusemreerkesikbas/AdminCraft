package com.backend.shared.common;

import java.util.regex.Pattern;

public final class LogSanitizer {

    private static final int DEFAULT_MAX_LENGTH = 500;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}");

    private LogSanitizer() {
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return "***";
        }

        int atIndex = email.indexOf('@');
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (local.isEmpty()) {
            return "***" + domain;
        }

        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }

        int visibleStart = Math.min(2, local.length());
        int visibleEnd = Math.min(2, local.length() - visibleStart);
        String start = local.substring(0, visibleStart);
        String end = visibleEnd > 0 ? local.substring(local.length() - visibleEnd) : "";
        return start + "****" + end + domain;
    }

    public static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (maxLength <= 0) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public static String sanitizeForLog(String value) {
        if (value == null) {
            return null;
        }
        String redacted = EMAIL_PATTERN.matcher(value).replaceAll("[REDACTED_EMAIL]");
        return truncate(redacted, DEFAULT_MAX_LENGTH);
    }

    public static String sanitizeException(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        return sanitizeForLog(throwable.getMessage());
    }
}
