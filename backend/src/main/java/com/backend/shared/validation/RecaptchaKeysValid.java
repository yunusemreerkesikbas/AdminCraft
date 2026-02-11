package com.backend.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that reCAPTCHA keys are provided when reCAPTCHA is enabled.
 * 
 * <p>Cross-field validation constraint for platform settings:
 * When {@code recaptchaEnabled == true}, both {@code recaptchaSiteKey} 
 * and {@code recaptchaSecretKey} must be non-null and non-blank.</p>
 * 
 * <p>Usage:</p>
 * <pre>{@code
 * @RecaptchaKeysValid
 * public record PatchPlatformSettingsRequest(
 *     Boolean recaptchaEnabled,
 *     String recaptchaSiteKey,
 *     String recaptchaSecretKey
 * ) {}
 * }</pre>
 * 
 * @see RecaptchaKeysValidator
 */
@Documented
@Constraint(validatedBy = RecaptchaKeysValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RecaptchaKeysValid {
    
    String message() default "{validation.recaptcha.keys.required}";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
