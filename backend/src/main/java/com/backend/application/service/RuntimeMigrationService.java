package com.backend.application.service;

import com.backend.domain.entity.EntryFieldDefinition;
import com.backend.domain.enums.EntryFieldType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuntimeMigrationService {

    private final JdbcTemplate jdbcTemplate;

    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "id", "uuid", "uid", "entry_id", "language", "title", "description",
            "image_url", "button_text", "button_url", "status", "published_at", "updated_at",
            "select", "from", "where", "insert", "update", "delete", "drop", "alter", "create"
    );

    private static final long MAX_TABLE_ROWS_WARNING = 1_000_000;

    @Transactional
    public void addFieldColumn(EntryFieldDefinition field) {
        validateMigrationSafety(field);
        
        String sql = generateAlterTableSql(field);
        
        log.info("Executing runtime migration: {}", sql);
        jdbcTemplate.execute(sql);
        log.info("Successfully added column: {}", field.getFieldKey());
    }

    private void validateMigrationSafety(EntryFieldDefinition field) {
        if (RESERVED_KEYWORDS.contains(field.getFieldKey().toLowerCase())) {
            throw new IllegalArgumentException("Field key is a reserved keyword: " + field.getFieldKey());
        }

        if (!field.getFieldKey().matches("^[a-z][a-zA-Z0-9]{0,49}$")) {
            throw new IllegalArgumentException("Invalid field key format: " + field.getFieldKey());
        }

        Boolean columnExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) > 0 FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'component_entry_i18n' " +
                        "AND COLUMN_NAME = ?",
                Boolean.class,
                field.getFieldKey()
        );

        if (Boolean.TRUE.equals(columnExists)) {
            throw new IllegalArgumentException("Column already exists: " + field.getFieldKey());
        }

        Long rowCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM component_entry_i18n", Long.class);

        if (rowCount != null && rowCount > MAX_TABLE_ROWS_WARNING) {
            log.warn("Table has {} rows. ALTER TABLE may take time.", rowCount);
        }
    }

    private String generateAlterTableSql(EntryFieldDefinition field) {
        String columnType = mapFieldTypeToSqlType(field);
        String nullable = field.getIsRequired() ? "NOT NULL" : "NULL";

        return String.format(
                "ALTER TABLE component_entry_i18n ADD COLUMN %s %s %s",
                field.getFieldKey(),
                columnType,
                nullable
        );
    }

    private String mapFieldTypeToSqlType(EntryFieldDefinition field) {
        return switch (field.getFieldType()) {
            case text -> {
                int maxLength = field.getMaxLength() != null ? field.getMaxLength() : 500;
                yield String.format("VARCHAR(%d)", maxLength);
            }
            case textarea -> "TEXT";
            case number -> "DECIMAL(10,2)";
            case bool -> "BOOLEAN";
        };
    }

    public boolean columnExists(String columnName) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) > 0 FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'component_entry_i18n' " +
                        "AND COLUMN_NAME = ?",
                Boolean.class,
                columnName
        );
        return Boolean.TRUE.equals(exists);
    }
}

