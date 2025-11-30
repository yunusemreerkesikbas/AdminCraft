package com.backend.domain.entity;

import com.backend.domain.enums.ComponentStatus;
import com.backend.infrastructure.persistence.converter.JsonNodeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "components", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_component_uid")
}, indexes = {
        @Index(columnList = "component_type_id", name = "idx_component_type"),
        @Index(columnList = "status", name = "idx_component_status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Component extends BaseEntity {

    @NotNull
    @Column(name = "component_type_id", nullable = false)
    private Long componentTypeId;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "base_data", columnDefinition = "JSON")
    private JsonNode baseData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private ComponentStatus status = ComponentStatus.DRAFT;
}
