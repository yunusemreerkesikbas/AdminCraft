package com.backend.application.service.impex;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.context.MessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.backend.application.dto.impex.ImpExResult;
import com.backend.application.dto.impex.StatementResult;
import com.backend.domain.exception.ImpExInvalidScriptException;
import com.backend.domain.port.TenantContextPort;
import com.backend.infrastructure.tenant.MultiTenantConnectionProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImpExServiceImpl implements ImpExService {

    private static final String IMPEX_MARKER = "-- #ADMINCRAFT_IMPEX";
    private static final int PREVIEW_LENGTH = 80;

    private static final Set<String> ALLOWED_KEYWORDS = Set.of("INSERT", "UPDATE", "SELECT");
    private static final Set<String> BLOCKED_KEYWORDS = Set.of(
        "DELETE", "DROP", "TRUNCATE", "ALTER", "CREATE", "RENAME", "REPLACE"
    );

    private final MultiTenantConnectionProvider multiTenantConnectionProvider;
    private final TenantContextPort tenantContext;
    private final MessageSource messageSource;

    @Override
    public ImpExResult execute(String sqlContent, Locale locale) {
        log.info("ImpEx execution started, content length: {} chars", sqlContent.length());

        validateMarker(sqlContent, locale);

        List<String> statements = parseStatements(sqlContent);
        log.debug("ImpEx parsed {} statement(s)", statements.size());

        JdbcTemplate jdbc = new JdbcTemplate(
            multiTenantConnectionProvider.getDataSource(tenantContext.getTenantDbName())
        );
        List<StatementResult> results = executeStatements(statements, locale, jdbc);

        int executed = (int) results.stream().filter(StatementResult::success).count();
        int failed = results.size() - executed;

        String status = failed == 0 ? "SUCCESS" : "PARTIAL";
        log.info("ImpEx execution finished — status: {}, total: {}, success: {}, failed: {}", status, results.size(), executed, failed);

        return new ImpExResult(status, results.size(), executed, failed, results, LocalDateTime.now());
    }

    private void validateMarker(String content, Locale locale) {
        if (!content.contains(IMPEX_MARKER)) {
            String msg = messageSource.getMessage("impex.error.marker.missing", null, locale);
            log.warn("ImpEx rejected: marker missing");
            throw new ImpExInvalidScriptException(msg);
        }
    }

    private List<String> parseStatements(String content) {
        String[] parts = content.split(";");
        List<String> statements = new ArrayList<>();
        for (String part : parts) {
            String trimmed = trimStatement(part);
            if (!trimmed.isEmpty()) {
                statements.add(trimmed);
            }
        }
        return statements;
    }

    private String trimStatement(String raw) {
        StringBuilder sb = new StringBuilder();
        for (String line : raw.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                sb.append(trimmed).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private List<StatementResult> executeStatements(List<String> statements, Locale locale, JdbcTemplate jdbc) {
        List<StatementResult> results = new ArrayList<>();
        for (int i = 0; i < statements.size(); i++) {
            String sql = statements.get(i);
            String preview = sql.length() > PREVIEW_LENGTH
                ? sql.substring(0, PREVIEW_LENGTH) + "..."
                : sql;

            String blockReason = checkBlocked(sql, locale);
            if (blockReason != null) {
                log.warn("ImpEx statement [{}] blocked — preview: {}", i + 1, preview);
                results.add(StatementResult.failure(i + 1, preview, blockReason));
                continue;
            }

            try {
                String upper = sql.stripLeading().toUpperCase(Locale.ROOT);
                if (upper.startsWith("SELECT")) {
                    List<?> rows = jdbc.queryForList(sql);
                    log.debug("ImpEx statement [{}] OK — {} row(s) returned", i + 1, rows.size());
                    results.add(StatementResult.success(i + 1, preview, rows.size()));
                } else {
                    int rows = jdbc.update(sql);
                    log.debug("ImpEx statement [{}] OK — {} row(s) affected", i + 1, rows);
                    results.add(StatementResult.success(i + 1, preview, rows));
                }
            } catch (Exception e) {
                log.error("ImpEx statement [{}] FAILED — {}", i + 1, e.getMessage(), e);
                results.add(StatementResult.failure(i + 1, preview, e.getMessage()));
            }
        }
        return results;
    }

    private String checkBlocked(String sql, Locale locale) {
        String upper = sql.stripLeading().toUpperCase(Locale.ROOT);

        for (String blocked : BLOCKED_KEYWORDS) {
            if (upper.startsWith(blocked)) {
                return messageSource.getMessage(
                    "impex.statement.blocked", new Object[]{ blocked }, locale
                );
            }
        }

        boolean allowed = ALLOWED_KEYWORDS.stream().anyMatch(upper::startsWith);
        if (!allowed) {
            return messageSource.getMessage("impex.statement.unknown", null, locale);
        }

        return null;
    }
}
