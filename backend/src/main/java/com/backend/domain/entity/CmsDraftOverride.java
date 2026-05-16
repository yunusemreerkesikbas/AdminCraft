package com.backend.domain.entity;

import com.backend.domain.enums.CmsDraftTargetType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cms_draft_overrides", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "target_type", "target_id", "language_key" }, name = "uk_cms_draft_target")
}, indexes = {
    @Index(columnList = "target_type, target_id", name = "idx_cms_draft_target")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CmsDraftOverride extends BaseEntity {

    public static final String NO_LANGUAGE = "_";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    private CmsDraftTargetType targetType;

    @NotNull
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @NotNull
    @Column(name = "language_key", nullable = false, length = 10)
    private String languageKey = NO_LANGUAGE;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "LONGTEXT")
    private String payload;
}
