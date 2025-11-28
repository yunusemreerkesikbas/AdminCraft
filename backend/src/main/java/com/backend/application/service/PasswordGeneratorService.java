package com.backend.application.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.stream.Collectors;

/**
 * Service for generating secure random passwords.
 * Sprint 20: Tenant Admin User Generator
 */
@Service
public class PasswordGeneratorService {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
    private static final int PASSWORD_LENGTH = 12;

    private final SecureRandom random = new SecureRandom();

    /**
     * Generate a secure random password with:
     * - Length: 12 characters
     * - At least 1 uppercase letter
     * - At least 1 lowercase letter
     * - At least 1 digit
     * - At least 1 special character (!@#$%^&*)
     *
     * @return randomly generated password
     */
    public String generate() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        // Ensure at least 1 of each required type
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // Fill remaining characters randomly
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // Shuffle to randomize position of required characters
        return shuffleString(password.toString());
    }

    /**
     * Shuffle the characters in a string using Fisher-Yates algorithm.
     *
     * @param input the string to shuffle
     * @return shuffled string
     */
    private String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            // Swap characters
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}
