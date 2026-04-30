package com.backend.application.dto.request;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.Size;

public final class EntryI18nUpdateCommand {

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
        if (!fieldNode.isTextual()) {
            throw new IllegalArgumentException(
                    "Field '" + fieldName + "' must be a string, got: " + fieldNode.getNodeType());
        }
        String trimmed = fieldNode.textValue().trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    private static Map<String, Object> readMapField(JsonNode node, String fieldName) {
        if (!hasField(node, fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        if (!fieldNode.isObject()) {
            throw new IllegalArgumentException(
                    "Field '" + fieldName + "' must be an object, got: " + fieldNode.getNodeType());
        }
        Map<String, Object> result = new LinkedHashMap<>();
        fieldNode.fields().forEachRemaining(entry -> result.put(entry.getKey(), toJavaValue(entry.getValue())));
        return result;
    }

    private static Object toJavaValue(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isTextual()) return node.textValue();
        if (node.isBoolean()) return node.booleanValue();
        if (node.isInt()) return node.intValue();
        if (node.isLong()) return node.longValue();
        if (node.isDouble() || node.isFloat()) return node.doubleValue();
        if (node.isObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            node.fields().forEachRemaining(e -> map.put(e.getKey(), toJavaValue(e.getValue())));
            return map;
        }
        if (node.isArray()) {
            java.util.List<Object> list = new java.util.ArrayList<>();
            node.forEach(item -> list.add(toJavaValue(item)));
            return list;
        }
        return node.asText();
    }
}
