package com.backend.infrastructure.email;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.backend.application.dto.email.EmailResult;
import com.backend.domain.port.TenantMailSenderPort;
import com.backend.shared.common.LogSanitizer;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantPostmarkEmailSender implements TenantMailSenderPort {

    private static final String POSTMARK_EMAIL_API = "https://api.postmarkapp.com/email";
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    public EmailResult send(
        String serverToken,
        String fromEmail,
        String fromName,
        String toEmail,
        String subject,
        String content
    ) {
        if (serverToken == null || serverToken.isBlank()) {
            return EmailResult.failure("Tenant Postmark server token is missing");
        }

        try {
            Map<String, Object> payload = Map.of(
                "From", String.format("%s <%s>", fromName, fromEmail),
                "To", toEmail,
                "Subject", subject,
                "HtmlBody", content
            );

            String requestBody = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(POSTMARK_EMAIL_API))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=utf-8")
                .header("X-Postmark-Server-Token", serverToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Map<?, ?> responseMap = objectMapper.readValue(response.body(), Map.class);
                Object messageId = responseMap.get("MessageID");
                return EmailResult.success(messageId != null ? messageId.toString() : "postmark-ok");
            }
            String safeBody = LogSanitizer.sanitizeForLog(response.body());
            log.warn("Postmark send failed: status={} body={}", response.statusCode(), safeBody);
            return EmailResult.failure("Postmark send failed with HTTP " + response.statusCode());
        } catch (Exception ex) {
            log.error("Postmark send failed due to exception", ex);
            return EmailResult.failure("Postmark send failed due to internal error");
        }
    }
}
