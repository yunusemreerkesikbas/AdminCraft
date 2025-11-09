package com.backend.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentSchemaValidator {

    private static final int MAX_SCHEMA_SIZE_BYTES = 100 * 1024; // 100KB
    private static final int MAX_FIELDS = 20;
    private static final int MAX_KEY_LENGTH = 50;
    private static final int MAX_LABEL_LENGTH = 100;
    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");
    private static final Set<String> VALID_TYPES = Set.of("text", "textarea", "number", "boolean", "select");

    private final ObjectMapper objectMapper;

    public JsonNode validateSchema(JsonNode schema) {
        log.debug("Starting schema validation");

        if (schema == null || schema.isNull()) {
            throw new IllegalArgumentException("Schema cannot be null");
        }
        String schemaString = schema.toString();
        if (schemaString.getBytes().length > MAX_SCHEMA_SIZE_BYTES) {
            throw new IllegalArgumentException("Schema size exceeds maximum allowed size of 100KB");
        }
        if (!schema.has("i18n")) {
            throw new IllegalArgumentException("Schema must contain 'i18n' array");
        }

        JsonNode i18nNode = schema.get("i18n");
        if (!i18nNode.isArray()) {
            throw new IllegalArgumentException("'i18n' must be an array");
        }

        ArrayNode i18nArray = (ArrayNode) i18nNode;
        if (i18nArray.isEmpty()) {
            throw new IllegalArgumentException("'i18n' array cannot be empty");
        }

        if (i18nArray.size() > MAX_FIELDS) {
            throw new IllegalArgumentException("Schema cannot contain more than " + MAX_FIELDS + " fields");
        }
        Set<String> keys = new HashSet<>();
        ArrayNode sanitizedArray = objectMapper.createArrayNode();

        for (JsonNode fieldNode : i18nArray) {
            String key = validateFieldDefinition(fieldNode, keys);
            keys.add(key);
            sanitizedArray.add(sanitizeFieldNode(fieldNode));
        }
        ObjectNode sanitizedSchema = objectMapper.createObjectNode();
        sanitizedSchema.set("i18n", sanitizedArray);

        log.debug("Schema validation completed successfully with {} fields", keys.size());
        return sanitizedSchema;
    }

    public String validateFieldDefinition(JsonNode field, Set<String> existingKeys) {
        if (field == null || !field.isObject()) {
            throw new IllegalArgumentException("Each field must be a valid object");
        }
        if (!field.has("key") || !field.get("key").isTextual()) {
            throw new IllegalArgumentException("Field must have a 'key' property of type string");
        }

        if (!field.has("type") || !field.get("type").isTextual()) {
            throw new IllegalArgumentException("Field must have a 'type' property of type string");
        }

        if (!field.has("label") || !field.get("label").isTextual()) {
            throw new IllegalArgumentException("Field must have a 'label' property of type string");
        }
        String key = field.get("key").asText();
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Field key cannot be empty");
        }

        if (key.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException("Field key cannot exceed " + MAX_KEY_LENGTH + " characters");
        }

        if (!KEY_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("Field key '" + key
                    + "' must start with a letter and contain only alphanumeric characters and underscores");
        }

        if (existingKeys.contains(key)) {
            throw new IllegalArgumentException("Duplicate field key found: " + key);
        }
        String type = field.get("type").asText();
        if (!VALID_TYPES.contains(type)) {
            throw new IllegalArgumentException("Invalid field type '" + type + "'. Allowed types: " + VALID_TYPES);
        }

        String label = field.get("label").asText();
        if (label.isEmpty()) {
            throw new IllegalArgumentException("Field label cannot be empty for key: " + key);
        }

        if (label.length() > MAX_LABEL_LENGTH) {
            throw new IllegalArgumentException(
                    "Field label cannot exceed " + MAX_LABEL_LENGTH + " characters for key: " + key);
        }

        validateTypeSpecificConstraints(field, key, type);

        return key;
    }

    private void validateTypeSpecificConstraints(JsonNode field, String key, String type) {
        switch (type) {
            case "text", "textarea" -> validateTextConstraints(field, key);
            case "number" -> validateNumberConstraints(field, key);
            case "select" -> validateSelectConstraints(field, key);
            case "boolean" -> validateBooleanConstraints(field, key);
        }
        if (field.has("required") && !field.get("required").isBoolean()) {
            throw new IllegalArgumentException("'required' constraint must be boolean for field: " + key);
        }
    }

    private void validateTextConstraints(JsonNode field, String key) {
        if (field.has("minLength")) {
            if (!field.get("minLength").isNumber()) {
                throw new IllegalArgumentException("'minLength' must be a number for field: " + key);
            }
            int minLength = field.get("minLength").asInt();
            if (minLength < 0) {
                throw new IllegalArgumentException("'minLength' cannot be negative for field: " + key);
            }
        }

        if (field.has("maxLength")) {
            if (!field.get("maxLength").isNumber()) {
                throw new IllegalArgumentException("'maxLength' must be a number for field: " + key);
            }
            int maxLength = field.get("maxLength").asInt();
            if (maxLength < 1) {
                throw new IllegalArgumentException("'maxLength' must be at least 1 for field: " + key);
            }

            if (field.has("minLength")) {
                int minLength = field.get("minLength").asInt();
                if (minLength > maxLength) {
                    throw new IllegalArgumentException(
                            "'minLength' cannot be greater than 'maxLength' for field: " + key);
                }
            }
        }

        if (field.has("pattern")) {
            if (!field.get("pattern").isTextual()) {
                throw new IllegalArgumentException("'pattern' must be a string for field: " + key);
            }
            String pattern = field.get("pattern").asText();
            try {
                Pattern.compile(pattern);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid regex pattern for field: " + key + " - " + e.getMessage());
            }
        }
    }

    private void validateNumberConstraints(JsonNode field, String key) {
        if (field.has("min")) {
            if (!field.get("min").isNumber()) {
                throw new IllegalArgumentException("'min' must be a number for field: " + key);
            }
        }

        if (field.has("max")) {
            if (!field.get("max").isNumber()) {
                throw new IllegalArgumentException("'max' must be a number for field: " + key);
            }

            if (field.has("min")) {
                double min = field.get("min").asDouble();
                double max = field.get("max").asDouble();
                if (min > max) {
                    throw new IllegalArgumentException("'min' cannot be greater than 'max' for field: " + key);
                }
            }
        }
    }

    private void validateSelectConstraints(JsonNode field, String key) {
        if (!field.has("options")) {
            throw new IllegalArgumentException("'select' type requires 'options' array for field: " + key);
        }

        JsonNode options = field.get("options");
        if (!options.isArray()) {
            throw new IllegalArgumentException("'options' must be an array for field: " + key);
        }

        if (options.isEmpty()) {
            throw new IllegalArgumentException("'options' array cannot be empty for field: " + key);
        }
        for (JsonNode option : options) {
            if (!option.isTextual()) {
                throw new IllegalArgumentException("All options must be strings for field: " + key);
            }
            if (option.asText().isEmpty()) {
                throw new IllegalArgumentException("Option values cannot be empty for field: " + key);
            }
        }
    }

    private void validateBooleanConstraints(JsonNode field, String key) {
        // Boolean type has no specific constraints beyond common ones
        // This method is here for consistency and future extensibility
    }

    private ObjectNode sanitizeFieldNode(JsonNode field) {
        ObjectNode sanitized = objectMapper.createObjectNode();
        sanitized.put("key", field.get("key").asText());
        sanitized.put("type", field.get("type").asText());
        String label = field.get("label").asText();
        sanitized.put("label", sanitizeValue(label));
        if (field.has("required")) {
            sanitized.put("required", field.get("required").asBoolean());
        }

        if (field.has("minLength")) {
            sanitized.put("minLength", field.get("minLength").asInt());
        }

        if (field.has("maxLength")) {
            sanitized.put("maxLength", field.get("maxLength").asInt());
        }

        if (field.has("min")) {
            sanitized.put("min", field.get("min").asDouble());
        }

        if (field.has("max")) {
            sanitized.put("max", field.get("max").asDouble());
        }

        if (field.has("pattern")) {
            sanitized.put("pattern", field.get("pattern").asText());
        }
        if (field.has("options")) {
            ArrayNode sanitizedOptions = objectMapper.createArrayNode();
            for (JsonNode option : field.get("options")) {
                sanitizedOptions.add(sanitizeValue(option.asText()));
            }
            sanitized.set("options", sanitizedOptions);
        }
        if (field.has("defaultValue")) {
            JsonNode defaultValue = field.get("defaultValue");
            if (defaultValue.isTextual()) {
                sanitized.put("defaultValue", sanitizeValue(defaultValue.asText()));
            } else if (defaultValue.isNumber()) {
                sanitized.put("defaultValue", defaultValue.asDouble());
            } else if (defaultValue.isBoolean()) {
                sanitized.put("defaultValue", defaultValue.asBoolean());
            }
        }

        return sanitized;
    }

    public String sanitizeValue(String value) {
        if (value == null) {
            return null;
        }
        return Encode.forHtml(value);
    }
}
