package com.backend.application.service;

import java.util.Set;

import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.EntryFieldDefinition;
import com.backend.infrastructure.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuntimeMigrationService {

    private final JdbcTemplate jdbcTemplate;
    private final TenantContext tenantContext;

    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "id", "uuid", "uid", "entry_id", "language", "title", "description",
            "image_url", "button_text", "button_url", "status", "published_at", "updated_at",
            "select", "from", "where", "insert", "update", "delete", "drop", "alter", "create");

    private static final long MAX_TABLE_ROWS_WARNING = 1_000_000;

    @Transactional
    public synchronized void addFieldColumn(EntryFieldDefinition field) {
        validateTenantContext();
        validateMigrationSafety(field);

        String sql = generateAlterTableSql(field);

        String correlationId = MDC.get("correlationId");
        log.info("[{}] Executing runtime migration: {}", correlationId, sql);

        jdbcTemplate.execute(sql);

        log.info("[{}] Successfully added column: {} for tenant: {}",
                correlationId, field.getFieldKey(), tenantContext.getTenantId());
    }

    protected void validateTenantContext() {
        String tenantId = tenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        String tenantDb = tenantContext.getTenantDbName();
        if (tenantDb == null || tenantDb.trim().isEmpty()) {
            throw new IllegalStateException("Tenant database name not set");
        }
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
                field.getFieldKey());

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

        String escapedColumnName = escapeIdentifier(field.getFieldKey());

        return String.format(
                "ALTER TABLE component_entry_i18n ADD COLUMN %s %s %s, ALGORITHM=INSTANT",
                escapedColumnName,
                columnType,
                nullable);
    }

    protected String escapeIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }

        String cleaned = identifier.replace("`", "``");
        return "`" + cleaned + "`";
    }

    private String mapFieldTypeToSqlType(EntryFieldDefinition field) {
        return switch (field.getFieldType()) {
            case TEXT -> {
                int maxLength = field.getMaxLength() != null ? field.getMaxLength() : 500;
                yield String.format("VARCHAR(%d)", maxLength);
            }
            case TEXTAREA -> "TEXT";
            case NUMBER -> "DECIMAL(10,2)";
            case BOOLEAN -> "BOOLEAN";
            case MEDIA -> "BIGINT"; // Stores ResponsiveMediaSet ID
        };
    }

    public boolean columnExists(String columnName) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) > 0 FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'component_entry_i18n' " +
                        "AND COLUMN_NAME = ?",
                Boolean.class,
                columnName);
        return Boolean.TRUE.equals(exists);
    }
}
