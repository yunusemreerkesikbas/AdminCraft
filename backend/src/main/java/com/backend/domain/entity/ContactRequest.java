package com.backend.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Tenant-scoped contact form submission. PII fields are excluded from {@link Object#toString()}
 * to reduce accidental leakage in logs. Rows older than the configured retention window may be
 * removed by the scheduled contact-request retention job.
 */
@Entity
@Table(name = "contact_requests")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@NoArgsConstructor
public class ContactRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 255)
    @ToString.Exclude
    private String fullName;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    @ToString.Exclude
    private String message;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "source", nullable = false, length = 64)
    private String source = "contact_page";

    @Column(name = "client_ip", length = 45)
    @ToString.Exclude
    private String clientIp;

    @Column(name = "user_agent", length = 500)
    @ToString.Exclude
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
