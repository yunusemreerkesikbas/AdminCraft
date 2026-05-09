package com.backend.application.cms.preview;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmsPreviewTicketService {

  private static final String HMAC_ALGO = "HmacSHA256";
  private static final char SEGMENT_SEPARATOR = '.';
  private static final char FIELD_SEPARATOR = '|';
  private static final int MIN_SECRET_BYTES = 32;

  private final CmsPreviewProperties properties;
  private SecretKeySpec secretKeySpec;

  @PostConstruct
  void validateSecret() {
    String secret = properties.getSecret();
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("app.cms.preview.secret must be configured");
    }
    if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
      throw new IllegalStateException(
          "app.cms.preview.secret must be at least " + MIN_SECRET_BYTES
              + " bytes (HMAC-SHA256 minimum)");
    }
    this.secretKeySpec = new SecretKeySpec(
        secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
  }

  public CmsPreviewTicket issue(long tenantId, Long userId, Long pageId) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(properties.getTtlSeconds());
    return new CmsPreviewTicket(tenantId, userId, pageId, now, exp);
  }

  public String encode(CmsPreviewTicket ticket) {
    String payload = serialize(ticket);
    String signature = sign(payload);
    return base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8))
        + SEGMENT_SEPARATOR
        + signature;
  }

  public Optional<CmsPreviewTicket> verify(String token) {
    if (token == null || token.isBlank()) {
      return Optional.empty();
    }
    int sep = token.indexOf(SEGMENT_SEPARATOR);
    if (sep <= 0 || sep == token.length() - 1) {
      return Optional.empty();
    }
    String payloadB64 = token.substring(0, sep);
    String signature = token.substring(sep + 1);

    String payload;
    try {
      payload = new String(base64UrlDecode(payloadB64), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException ex) {
      log.debug("Preview ticket payload base64 decode failed");
      return Optional.empty();
    }

    String expectedSig = sign(payload);
    if (!constantTimeEquals(signature, expectedSig)) {
      log.debug("Preview ticket signature mismatch");
      return Optional.empty();
    }

    Optional<CmsPreviewTicket> ticket = parse(payload);
    if (ticket.isEmpty()) {
      return Optional.empty();
    }

    if (ticket.get().isExpired(Instant.now())) {
      log.debug("Preview ticket expired (exp={})", ticket.get().expiresAt());
      return Optional.empty();
    }
    return ticket;
  }

  private String serialize(CmsPreviewTicket ticket) {
    long pageId = ticket.pageId() != null ? ticket.pageId() : 0L;
    long userId = ticket.userId() != null ? ticket.userId() : 0L;
    StringBuilder sb = new StringBuilder(64);
    sb.append(ticket.tenantId()).append(FIELD_SEPARATOR)
        .append(userId).append(FIELD_SEPARATOR)
        .append(pageId).append(FIELD_SEPARATOR)
        .append(ticket.issuedAt().getEpochSecond()).append(FIELD_SEPARATOR)
        .append(ticket.expiresAt().getEpochSecond());
    return sb.toString();
  }

  private Optional<CmsPreviewTicket> parse(String payload) {
    String[] parts = payload.split("\\|");
    if (parts.length != 5) {
      return Optional.empty();
    }
    try {
      long tenantId = Long.parseLong(parts[0]);
      long userId = Long.parseLong(parts[1]);
      long pageId = Long.parseLong(parts[2]);
      Instant issuedAt = Instant.ofEpochSecond(Long.parseLong(parts[3]));
      Instant expiresAt = Instant.ofEpochSecond(Long.parseLong(parts[4]));
      return Optional.of(new CmsPreviewTicket(
          tenantId,
          userId == 0L ? null : userId,
          pageId == 0L ? null : pageId,
          issuedAt,
          expiresAt));
    } catch (NumberFormatException ex) {
      log.debug("Preview ticket payload parse failed");
      return Optional.empty();
    }
  }

  private String sign(String payload) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGO);
      mac.init(this.secretKeySpec);
      byte[] sig = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
      return base64UrlEncode(sig);
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("Failed to sign CMS preview ticket", ex);
    }
  }

  private static String base64UrlEncode(byte[] bytes) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static byte[] base64UrlDecode(String s) {
    return Base64.getUrlDecoder().decode(s);
  }

  private static boolean constantTimeEquals(String a, String b) {
    byte[] ab = a.getBytes(StandardCharsets.UTF_8);
    byte[] bb = b.getBytes(StandardCharsets.UTF_8);
    return MessageDigest.isEqual(ab, bb);
  }
}
