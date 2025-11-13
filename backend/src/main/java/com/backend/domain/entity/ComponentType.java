package com.backend.domain.entity;

import com.backend.infrastructure.persistence.converter.JsonNodeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "component_types", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "code" }, name = "uk_component_type_code"),
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_component_type_uid")
}, indexes = {
        @Index(columnList = "category", name = "idx_component_type_category"),
        @Index(columnList = "is_system", name = "idx_component_type_system")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ComponentType extends BaseEntity {

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 50)
    @Column(length = 50)
    private String category;

    @Size(max = 50)
    @Column(length = 50)
    private String icon;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;
}
