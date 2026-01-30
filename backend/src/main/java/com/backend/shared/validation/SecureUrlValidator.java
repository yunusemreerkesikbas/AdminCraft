package com.backend.shared.validation;

import java.util.Set;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * OWASP-compliant URL validator that only allows whitelisted domains
 * This prevents URL injection attacks and ensures only trusted domains are
 * accepted
 */
public class SecureUrlValidator implements ConstraintValidator<SecureUrl, String> {

  // Whitelisted domains for different URL types

  private static final Set<String> ALLOWED_SOCIAL_DOMAINS = Set.of(
      "facebook.com", "www.facebook.com",
      "instagram.com", "www.instagram.com",
      "twitter.com", "www.twitter.com", "x.com", "www.x.com",
      "linkedin.com", "www.linkedin.com",
      "youtube.com", "www.youtube.com",
      "tiktok.com", "www.tiktok.com");

  // Basic URL pattern validation
  private static final Pattern URL_PATTERN = Pattern.compile(
      "^https?://([a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(:[0-9]+)?(/.*)?$");

  private SecureUrlType urlType;

  @Override
  public void initialize(SecureUrl annotation) {
    this.urlType = annotation.type();
  }

  @Override
  public boolean isValid(String url, ConstraintValidatorContext context) {
    if (url == null || url.trim().isEmpty()) {
      return true; // Let @NotNull handle null validation
    }

    // Basic URL format validation
    if (!URL_PATTERN.matcher(url).matches()) {
      return false;
    }

    // Extract domain from URL
    String domain = extractDomain(url);
    if (domain == null) {
      return false;
    }

    // Check against appropriate whitelist
    Set<String> allowedDomains = switch (urlType) {
      case SOCIAL_MEDIA -> ALLOWED_SOCIAL_DOMAINS;
      default -> null; // Allow all domains for CANONICAL and GENERAL
    };

    // If no whitelist is enforced, we accept the domain
    if (allowedDomains == null) {
      return true;
    }

    // Check if domain is in whitelist
    boolean isAllowed = allowedDomains.stream()
        .anyMatch(allowedDomain -> domain.equals(allowedDomain) ||
            domain.endsWith("." + allowedDomain));

    if (!isAllowed) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
          "URL domain '" + domain + "' is not in the allowed whitelist").addConstraintViolation();
    }

    return isAllowed;
  }

  private String extractDomain(String url) {
    try {
      // Remove protocol
      String withoutProtocol = url.replaceFirst("^https?://", "");

      // Extract domain part (before first slash or colon for port)
      int slashIndex = withoutProtocol.indexOf('/');
      int colonIndex = withoutProtocol.indexOf(':');

      int endIndex = Math.min(
          slashIndex == -1 ? withoutProtocol.length() : slashIndex,
          colonIndex == -1 ? withoutProtocol.length() : colonIndex);

      return withoutProtocol.substring(0, endIndex).toLowerCase();
    } catch (Exception e) {
      return null;
    }
  }
}
