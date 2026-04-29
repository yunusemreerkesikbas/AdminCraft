package com.backend.application.dto.request;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.Size;

public final class EntryI18nUpdateCommand {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    @Size(max = 255, message = "{validation.component.entry.title.size}")
    private final String title;

    @Size(max = 5000, message = "{validation.component.entry.description.size}")
    private final String description;

    @Size(max = 50, message = "{validation.component.entry.dynamic.fields.size}")
    private final Map<String, Object> dynamicFields;

    @JsonIgnore
    private final boolean titlePresent;

    @JsonIgnore
    private final boolean descriptionPresent;

    @JsonIgnore
    private final boolean dynamicFieldsPresent;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public EntryI18nUpdateCommand(JsonNode node) {
        this.titlePresent = hasField(node, "title");
        this.descriptionPresent = hasField(node, "description");
        this.dynamicFieldsPresent = hasField(node, "dynamicFields");
        this.title = readTextField(node, "title");
        this.description = readTextField(node, "description");
        this.dynamicFields = readMapField(node, "dynamicFields");
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public Map<String, Object> dynamicFields() {
        return dynamicFields;
    }

    public boolean hasTitle() {
        return titlePresent;
    }

    public boolean hasDescription() {
        return descriptionPresent;
    }

    public boolean hasDynamicFields() {
        return dynamicFieldsPresent;
    }

    private static boolean hasField(JsonNode node, String fieldName) {
        return node != null && node.has(fieldName);
    }

    private static String readTextField(JsonNode node, String fieldName) {
        if (!hasField(node, fieldName)) {
            return null;
        }

        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }

        String value = fieldNode.asText();
        String trimmed = value == null ? null : value.trim();
        return trimmed == null ? null : (trimmed.isEmpty() ? "" : trimmed);
    }

    private static Map<String, Object> readMapField(JsonNode node, String fieldName) {
        if (!hasField(node, fieldName)) {
            return null;
        }

        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }

        return OBJECT_MAPPER.convertValue(fieldNode, MAP_TYPE);
    }
}
