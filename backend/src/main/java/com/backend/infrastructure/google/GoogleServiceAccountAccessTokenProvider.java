package com.backend.infrastructure.google;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.backend.infrastructure.analytics.GoogleAnalyticsProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoogleServiceAccountAccessTokenProvider {

    private static final Pattern PEM_HEADER_FOOTER = Pattern.compile(
            "-----BEGIN PRIVATE KEY-----|-----END PRIVATE KEY-----|\\s+");

    private final GoogleAnalyticsProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, CachedToken> tokenCache = new ConcurrentHashMap<>();

    public String getAccessToken(String scope) {
        if (!StringUtils.hasText(scope)) {
            throw new IllegalArgumentException("Google OAuth scope is required");
        }

        CachedToken cachedToken = tokenCache.get(scope);
        Instant now = Instant.now();
        if (cachedToken != null && cachedToken.expiresAt().isAfter(now.plusSeconds(30))) {
            return cachedToken.accessToken();
        }

        synchronized (this) {
            cachedToken = tokenCache.get(scope);
            now = Instant.now();
            if (cachedToken != null && cachedToken.expiresAt().isAfter(now.plusSeconds(30))) {
                return cachedToken.accessToken();
            }

            CachedToken refreshedToken = requestAccessToken(scope);
            tokenCache.put(scope, refreshedToken);
            return refreshedToken.accessToken();
        }
    }

    private CachedToken requestAccessToken(String scope) {
        ServiceAccountCredentials credentials = readCredentials();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(3600);

        String assertion = buildAssertion(credentials, scope, now, expiresAt);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        body.add("assertion", assertion);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<String> response = restTemplate.postForEntity(
                credentials.tokenUri(),
                new HttpEntity<>(body, headers),
                String.class);

        try {
            JsonNode node = objectMapper.readTree(response.getBody());
            String accessToken = node.path("access_token").asText(null);
            int expiresIn = node.path("expires_in").asInt(3600);
            if (!StringUtils.hasText(accessToken)) {
                throw new IllegalStateException("Google OAuth response did not include access_token");
            }
            return new CachedToken(accessToken, now.plusSeconds(Math.max(expiresIn, 60)));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse Google OAuth token response", ex);
        }
    }

    private ServiceAccountCredentials readCredentials() {
        String json = properties.resolveServiceAccountJson();
        if (!StringUtils.hasText(json)) {
            throw new IllegalStateException("Google service account credentials are not configured");
        }

        try {
            JsonNode node = objectMapper.readTree(json);
            String clientEmail = node.path("client_email").asText(null);
            String privateKey = node.path("private_key").asText(null);
            String tokenUri = node.path("token_uri").asText(properties.getTokenUri());
            if (!StringUtils.hasText(clientEmail) || !StringUtils.hasText(privateKey)) {
                throw new IllegalStateException("Service account JSON is missing client_email or private_key");
            }
            return new ServiceAccountCredentials(clientEmail, privateKey, tokenUri);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse Google service account credentials", ex);
        }
    }

    private String buildAssertion(
            ServiceAccountCredentials credentials,
            String scope,
            Instant issuedAt,
            Instant expiresAt
    ) {
        try {
            String header = base64Url(objectMapper.writeValueAsBytes(Map.of(
                    "alg", "RS256",
                    "typ", "JWT")));
            String payload = base64Url(objectMapper.writeValueAsBytes(Map.of(
                    "iss", credentials.clientEmail(),
                    "scope", scope,
                    "aud", credentials.tokenUri(),
                    "iat", issuedAt.getEpochSecond(),
                    "exp", expiresAt.getEpochSecond())));

            String signingInput = header + "." + payload;
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(loadPrivateKey(credentials.privateKey()));
            signature.update(signingInput.getBytes(StandardCharsets.UTF_8));
            return signingInput + "." + base64Url(signature.sign());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build Google JWT assertion", ex);
        }
    }

    private PrivateKey loadPrivateKey(String pemValue) {
        try {
            String normalized = pemValue.replace("\\n", "\n");
            String encoded = PEM_HEADER_FOOTER.matcher(normalized).replaceAll("");
            byte[] keyBytes = Base64.getDecoder().decode(encoded);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load Google private key", ex);
        }
    }

    private String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private record ServiceAccountCredentials(
            String clientEmail,
            String privateKey,
            String tokenUri
    ) {
    }

    private record CachedToken(
            String accessToken,
            Instant expiresAt
    ) {
    }
}
