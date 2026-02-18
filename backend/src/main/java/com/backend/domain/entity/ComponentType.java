package com.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.backend.domain.enums.ComponentNavigationProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "component_types", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_component_type_uid")
}, indexes = {
        @Index(columnList = "category", name = "idx_component_type_category")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ComponentType extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 50)
    @Column(length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "navigation_profile", nullable = false, length = 40)
    private ComponentNavigationProfile navigationProfile = ComponentNavigationProfile.NONE;
}
