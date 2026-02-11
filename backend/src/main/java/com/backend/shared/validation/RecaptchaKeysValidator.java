package com.backend.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;

/**
 * Validator for {@link RecaptchaKeysValid} annotation.
 * 
 * <p>Validates that when reCAPTCHA is enabled, both site key and secret key are provided.</p>
 * 
 * <p>Uses reflection to work with any object that has:</p>
 * <ul>
 *   <li>{@code recaptchaEnabled()}: Boolean getter</li>
 *   <li>{@code recaptchaSiteKey()}: String getter</li>
 *   <li>{@code recaptchaSecretKey()}: String getter</li>
 * </ul>
 * 
 * <p>Validation logic:</p>
 * <ul>
 *   <li>If {@code recaptchaEnabled} is {@code false} or {@code null}: validation passes</li>
 *   <li>If {@code recaptchaEnabled} is {@code true}: both keys must be non-null and non-blank</li>
 * </ul>
 */
public class RecaptchaKeysValidator 
        implements ConstraintValidator<RecaptchaKeysValid, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }

        try {
            // Get recaptchaEnabled value via reflection
            Method enabledMethod = obj.getClass().getMethod("recaptchaEnabled");
            Boolean enabled = (Boolean) enabledMethod.invoke(obj);

            // If reCAPTCHA is not enabled, no validation needed
            if (!Boolean.TRUE.equals(enabled)) {
                return true;
            }

            // reCAPTCHA enabled: both keys must be provided
            Method siteKeyMethod = obj.getClass().getMethod("recaptchaSiteKey");
            Method secretKeyMethod = obj.getClass().getMethod("recaptchaSecretKey");

            String siteKey = (String) siteKeyMethod.invoke(obj);
            String secretKey = (String) secretKeyMethod.invoke(obj);

            boolean siteKeyValid = siteKey != null && !siteKey.isBlank();
            boolean secretKeyValid = secretKey != null && !secretKey.isBlank();

            return siteKeyValid && secretKeyValid;

        } catch (Exception e) {
            // If reflection fails, skip validation (bean structure not as expected)
            return true;
        }
    }
}
