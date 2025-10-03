package com.backend.domain.entity;

import com.backend.domain.enums.Language;
import com.backend.infrastructure.util.UuidUidGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Abstract base entity for internationalization (i18n) entities.
 *
 * <p>
 * This class provides common fields for language-specific content following
 * the Multi-Language Page Builder architecture:
 * <ul>
 * <li>id: Primary key (auto-generated)</li>
 * <li>uuid: Server-generated UUID for external references</li>
 * <li>uid: Human-readable stable identifier (auto-generated with "i18n" prefix
 * if not provided)</li>
 * <li>tenantId: Multi-tenant support</li>
 * <li>language: Language code (e.g., TR, EN)</li>
 * <li>updatedAt: Last update timestamp</li>
 * </ul>
 *
 * <p>
 * This base class is designed for i18n side tables following the pattern:
 * {@code <entity>_i18n} with consistent columns for all multi-language content.
 *
 * <p>
 * The {@link PrePersist} hook automatically sets uuid, uid (with "i18n"
 * prefix),
 * and timestamp fields on creation. The {@link PreUpdate} hook automatically
 * updates
 * the updatedAt timestamp.
 *
 * <p>
 * UID generation:
 * <ul>
 * <li>If uid is null or empty on persist, generates "i18n_XXXXXXXX" format</li>
 * <li>Can be overridden before persist by setting a custom uid</li>
 * <li>Should be immutable after creation (enforced at business layer)</li>
 * </ul>
 *
 * @author AdminCraft Team
 * @version 1.0
 * @since 1.0
 */
@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseI18nEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "uid", nullable = false, unique = true, length = 50)
    private String uid;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 10)
    private Language language;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (uuid == null) {
            uuid = UuidUidGenerator.generateUuid();
        }

        if (uid == null || uid.trim().isEmpty()) {
            uid = UuidUidGenerator.generateUid("i18n");
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
