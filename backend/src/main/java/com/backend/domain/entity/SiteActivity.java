package com.backend.domain.entity;

import com.backend.domain.enums.ActivityAction;
import com.backend.domain.enums.ActivityEntityType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing site activity for tracking user actions.
 * Used for the Overview tab in Site Dashboard.
 */
@Entity
@Table(name = "site_activity", indexes = {
                @Index(name = "idx_site_activity_created_at", columnList = "created_at DESC"),
                @Index(name = "idx_site_activity_entity_type", columnList = "entity_type"),
                @Index(name = "idx_site_activity_user_id", columnList = "user_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SiteActivity extends BaseEntity {

        @NotNull
        @Enumerated(EnumType.STRING)
        @Column(name = "action", nullable = false, length = 20)
        private ActivityAction action;

        @NotNull
        @Enumerated(EnumType.STRING)
        @Column(name = "entity_type", nullable = false, length = 20)
        private ActivityEntityType entityType;

        @Column(name = "entity_id")
        private Long entityId;

        @Size(max = 255)
        @Column(name = "entity_name", length = 255)
        private String entityName;

        @Column(name = "user_id")
        private Long userId;

        @Size(max = 500)
        @Column(name = "description", length = 500)
        private String description;

        /**
         * Factory method for creating a new activity record.
         */
        public static SiteActivity create(
                        ActivityAction action,
                        ActivityEntityType entityType,
                        Long entityId,
                        String entityName,
                        Long userId) {
                return SiteActivity.builder()
                                .action(action)
                                .entityType(entityType)
                                .entityId(entityId)
                                .entityName(entityName)
                                .userId(userId)
                                .build();
        }
}
