package com.backend.shared.util;

/**
 * Utility class for generating code (slug) from name.
 * Converts Turkish characters and special characters to safe code format.
 */
public final class SlugGenerator {

    private SlugGenerator() {
        // Prevent instantiation
    }

    /**
     * Generates a code (slug) from a name.
     * 
     * Rules:
     * - Converts to lowercase
     * - Replaces Turkish characters (ı→i, ğ→g, ü→u, ş→s, ö→o, ç→c)
     * - Removes special characters (keeps only alphanumeric, spaces, hyphens)
     * - Replaces spaces and hyphens with underscores
     * - Removes leading/trailing underscores
     * - Ensures code starts with a letter (if name starts with number, prepends 'field_')
     * 
     * @param name The name to convert
     * @return Generated code
     */
    public static String generateCodeFromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }

        String code = name
                .toLowerCase()
                .trim()
                // Replace Turkish characters
                .replace("ı", "i")
                .replace("ğ", "g")
                .replace("ü", "u")
                .replace("ş", "s")
                .replace("ö", "o")
                .replace("ç", "c")
                .replace("İ", "i")
                .replace("Ğ", "g")
                .replace("Ü", "u")
                .replace("Ş", "s")
                .replace("Ö", "o")
                .replace("Ç", "c")
                // Remove special characters (keep only alphanumeric, spaces, hyphens, underscores)
                .replaceAll("[^a-z0-9\\s_-]", "")
                // Replace multiple spaces/hyphens with single underscore
                .replaceAll("[\\s-]+", "_")
                // Remove leading/trailing underscores
                .replaceAll("^_+|_+$", "");

        // Ensure code starts with a letter (code pattern requirement: ^[a-z][a-z0-9_]*$)
        if (!code.isEmpty() && Character.isDigit(code.charAt(0))) {
            code = "field_" + code;
        }

        // If code is empty after processing, return empty string
        if (code.isEmpty()) {
            return "";
        }

        // Ensure code doesn't exceed max length (50 for CODE_PATTERN)
        if (code.length() > 50) {
            code = code.substring(0, 50);
            // Remove trailing underscore if truncated
            code = code.replaceAll("_+$", "");
        }

        return code;
    }

    /**
     * Generates a unique code by appending a suffix if the base code already exists.
     * 
     * @param baseCode The base code
     * @param existsChecker Function to check if code exists
     * @return Unique code
     */
    public static String generateUniqueCode(String baseCode, java.util.function.Function<String, Boolean> existsChecker) {
        if (baseCode == null || baseCode.isEmpty()) {
            return "";
        }

        String code = baseCode;
        int suffix = 1;

        while (existsChecker.apply(code)) {
            // Append suffix: base_code_1, base_code_2, etc.
            String suffixStr = "_" + suffix;
            int maxLength = 50 - suffixStr.length();
            if (maxLength <= 0) {
                // Fallback: use timestamp if code is too long
                code = "field_" + System.currentTimeMillis();
                break;
            }
            String truncatedBase = baseCode.length() > maxLength 
                    ? baseCode.substring(0, maxLength).replaceAll("_+$", "")
                    : baseCode;
            code = truncatedBase + suffixStr;
            suffix++;
        }

        return code;
    }
}
