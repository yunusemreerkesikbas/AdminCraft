package com.backend.application.dto.request;

import com.backend.domain.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePageRequest(
        @NotNull Long id,
        @NotNull Long tenantId,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 200) @Pattern(regexp = "[a-z0-9-]+", message = "validation.slug.pattern") String slug,
        @NotNull Language language,
        @Size(max = 60) String metaTitle,
        @Size(max = 160) String metaDescription,
        @Size(max = 255) @Pattern(regexp = "https?://.+", message = "validation.url.invalid") String canonicalUrl,
        // Açıklama: Kısa alt başlık
        @Size(max = 200) String subtitle,
        // Açıklama: CSS sınıf isimleri (boşlukla ayrılmış)
        @Size(max = 255) String styleClasses,
        // Açıklama: Zengin metin açıklama (HTML/Delta)
        String description,
        String descriptionHtml,
        // Açıklama: Öne çıkan görsel URL/anahtar
        @Size(max = 500) String featuredImage) {
    public UpdatePageRequest {
        if (title != null)
            title = title.trim();
        if (slug != null)
            slug = slug.toLowerCase();
    }
}
