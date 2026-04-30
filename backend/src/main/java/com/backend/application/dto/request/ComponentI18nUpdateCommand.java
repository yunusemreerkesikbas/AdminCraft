package com.backend.application.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.Size;

public final class ComponentI18nUpdateCommand {

    @Size(max = 200, message = "{validation.component.title.size}")
    private final String title;

    @Size(max = 200, message = "{validation.component.subtitle.size}")
    private final String subtitle;

    @Size(max = 5000, message = "{validation.component.description.size}")
    private final String description;

    @JsonIgnore
    private final boolean titlePresent;

    @JsonIgnore
    private final boolean subtitlePresent;

    @JsonIgnore
    private final boolean descriptionPresent;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ComponentI18nUpdateCommand(JsonNode node) {
        this.titlePresent = hasField(node, "title");
        this.subtitlePresent = hasField(node, "subtitle");
        this.descriptionPresent = hasField(node, "description");
        this.title = readTextField(node, "title");
        this.subtitle = readTextField(node, "subtitle");
        this.description = readTextField(node, "description");
    }

    public String title() {
        return title;
    }

    public String subtitle() {
        return subtitle;
    }

    public String description() {
        return description;
    }

    public boolean hasTitle() {
        return titlePresent;
    }

    public boolean hasSubtitle() {
        return subtitlePresent;
    }

    public boolean hasDescription() {
        return descriptionPresent;
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
            return null;
        }
        String trimmed = fieldNode.textValue().trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }
}
