package com.backend.application.service.impex;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.backend.application.dto.impex.ImpExResult;
import com.backend.application.dto.impex.StatementResult;
import com.backend.domain.entity.ImpExAudit;
import com.backend.domain.exception.ImpExInvalidScriptException;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.ImpExAuditRepository;
import com.backend.infrastructure.tenant.MultiTenantConnectionProvider;
import com.backend.shared.common.SecurityHelper;
import com.backend.shared.common.SqlExceptionTranslator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ImpExServiceImpl implements ImpExService {

    private static final String IMPEX_MARKER = "-- #CRAFTIVE_IMPEX";
    private static final int PREVIEW_LENGTH = 80;
    static final int MAX_AUDIT_SQL_LENGTH = 1_048_576;
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_PARTIAL = "PARTIAL";
    private static final String STATUS_REJECTED = "REJECTED";
    // SEC-101: word-boundary keyword detection (prevents `/* x */DROP` and similar bypass).
    private static final Pattern BLOCKED_PREFIX = Pattern.compile(
        "^\\s*\\b(DELETE|DROP|TRUNCATE|ALTER|CREATE|RENAME|REPLACE)\\b",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern ALLOWED_PREFIX = Pattern.compile(
        "^\\s*\\b(INSERT|UPDATE|SELECT)\\b",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern SELECT_PREFIX = Pattern.compile(
        "^\\s*\\bSELECT\\b",
        Pattern.CASE_INSENSITIVE);
    // SEC-100: tables that hold authn/authz/tenancy state and must never be mutated via ImpEx.
    private static final Set<String> SENSITIVE_TABLES = Set.of(
        "users", "platform_admin_users", "tenants", "tenant_modules",
        "config_properties", "platform_settings", "platform_refresh_tokens"
    );
    /** Captures full first table reference (quoted identifiers and schema.table segments). */
    private static final Pattern DML_TARGET_TABLE = Pattern.compile(
        "^\\s*(?:INSERT(?:\\s+IGNORE)?\\s+INTO|UPDATE(?:\\s+IGNORE)?)\\s+"
            + "((?:`[^`]+`|[\"][^\"]+\"|\\w+)(?:\\s*\\.\\s*(?:`[^`]+`|[\"][^\"]+\"|\\w+))*)",
        Pattern.CASE_INSENSITIVE);

    private final MultiTenantConnectionProvider multiTenantConnectionProvider;
    private final TenantContextPort tenantContext;
    private final MessageSource messageSource;
    private final ImpExAuditRepository impExAuditRepository;
    private final SecurityHelper securityHelper;

    public ImpExServiceImpl(
            MultiTenantConnectionProvider multiTenantConnectionProvider,
            TenantContextPort tenantContext,
            MessageSource messageSource,
            ImpExAuditRepository impExAuditRepository,
            SecurityHelper securityHelper) {
        this.multiTenantConnectionProvider = multiTenantConnectionProvider;
        this.tenantContext = tenantContext;
        this.messageSource = messageSource;
        this.impExAuditRepository = impExAuditRepository;
        this.securityHelper = securityHelper;
    }

    @Override
    public ImpExResult execute(String sqlContent, Locale locale, String clientIp) {
        log.info("ImpEx execution started, content length: {} chars", sqlContent.length());

        Instant start = Instant.now();
        int total = 0;
        int executed = 0;
        int failed = 0;
        String status = STATUS_REJECTED;

        try {
            validateMarker(sqlContent, locale);
            rejectConditionalComment(sqlContent, locale);

            List<String> statements = parseStatements(sqlContent);
            total = statements.size();
            log.debug("ImpEx parsed {} statement(s)", total);

            DataSource ds = resolveDataSource(locale);
            JdbcTemplate jdbc = new JdbcTemplate(ds);
            List<StatementResult> results = executeStatements(statements, locale, jdbc);

            executed = (int) results.stream().filter(StatementResult::success).count();
            failed = results.size() - executed;

            status = failed == 0 ? STATUS_SUCCESS : STATUS_PARTIAL;
            log.info("ImpEx execution finished — status: {}, total: {}, success: {}, failed: {}", status, results.size(), executed, failed);

            return new ImpExResult(status, results.size(), executed, failed, results, LocalDateTime.now());
        } finally {
            long durationMs = Duration.between(start, Instant.now()).toMillis();
            writeAuditSafely(sqlContent, total, executed, failed, status, clientIp, durationMs);
        }
    }

    private void writeAuditSafely(String sqlContent, int total, int success, int failed,
                                  String status, String clientIp, long durationMs) {
        try {
            String fullSql = sqlContent != null && sqlContent.length() > MAX_AUDIT_SQL_LENGTH
                    ? sqlContent.substring(0, MAX_AUDIT_SQL_LENGTH)
                    : sqlContent;

            ImpExAudit audit = ImpExAudit.builder()
                    .actorUserId(securityHelper.getCurrentUserIdOrNull())
                    .actorEmail(safeCurrentUserEmail())
                    .actorRole(safeCurrentUserRole())
                    .tenantId(parseLongOrNull(tenantContext.getTenantId()))
                    .tenantDb(tenantContext.getTenantDbName())
                    .fullSql(fullSql)
                    .statementCount(total)
                    .successCount(success)
                    .failedCount(failed)
                    .status(status)
                    .correlationId(MDC.get("correlationId"))
                    .clientIp(clientIp)
                    .durationMs((int) Math.min(durationMs, Integer.MAX_VALUE))
                    .build();

            impExAuditRepository.save(audit);
        } catch (Exception e) {
            log.error("Failed to write ImpEx audit row — execution flow continues", e);
        }
    }

    private String safeCurrentUserEmail() {
        try {
            return securityHelper.getCurrentUserEmail();
        } catch (AccessDeniedException ignored) {
            return null;
        }
    }

    private String safeCurrentUserRole() {
        try {
            return securityHelper.isSuperAdmin() ? "SUPER_ADMIN" : "TENANT_ADMIN";
        } catch (AccessDeniedException | AuthenticationException ex) {
            log.warn("ImpEx audit: unable to resolve actor role", ex);
            return null;
        }
    }

    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private DataSource resolveDataSource(Locale locale) {
        // SEC-002: Platform DB fallback removed. ImpEx requires explicit tenant context for
        // every actor (incl. SUPER_ADMIN). Platform-DB seed data is applied via Flyway.
        String tenantDbName = tenantContext.getTenantDbName();
        if (tenantDbName == null || tenantDbName.isBlank()) {
            throw new IllegalStateException(
                messageSource.getMessage("impex.error.tenant.required", null, locale));
        }
        return multiTenantConnectionProvider.getDataSource(tenantDbName);
    }

    private void validateMarker(String content, Locale locale) {
        if (!content.contains(IMPEX_MARKER)) {
            String msg = messageSource.getMessage("impex.error.marker.missing", null, locale);
            log.warn("ImpEx rejected: marker missing");
            throw new ImpExInvalidScriptException(msg);
        }
    }

    private List<String> parseStatements(String content) {
        // SEC-101: strip comments BEFORE splitting on `;` so a `;` inside `/* ... */`
        // can't break statement boundaries, and so checkBlocked / sensitiveTableTarget
        // run against comment-free SQL.
        String stripped = stripComments(content);
        String[] parts = splitOnUnquotedSemicolons(stripped);
        List<String> statements = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                statements.add(trimmed);
            }
        }
        return statements;
    }

    // SEC-101: strips line (--) and block comments while preserving string-literal contents.
    // Each comment is replaced with a single space so adjacent tokens stay separated.
    // Single-quoted strings (with `''` escapes) pass through verbatim.
    static String stripComments(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder(content.length());
        boolean inString = false;
        int i = 0;
        int n = content.length();
        while (i < n) {
            char c = content.charAt(i);
            if (inString) {
                out.append(c);
                if (c == '\'') {
                    if (i + 1 < n && content.charAt(i + 1) == '\'') {
                        out.append('\'');
                        i += 2;
                        continue;
                    }
                    inString = false;
                }
                i++;
                continue;
            }
            if (c == '\'') {
                inString = true;
                out.append(c);
                i++;
                continue;
            }
            if (c == '-' && i + 1 < n && content.charAt(i + 1) == '-') {
                while (i < n && content.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }
            if (c == '/' && i + 1 < n && content.charAt(i + 1) == '*') {
                i += 2;
                boolean closed = false;
                while (i + 1 < n) {
                    if (content.charAt(i) == '*' && content.charAt(i + 1) == '/') {
                        i += 2;
                        closed = true;
                        break;
                    }
                    i++;
                }
                if (!closed) {
                    i = n;
                }
                out.append(' ');
                continue;
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    private static String[] splitOnUnquotedSemicolons(String content) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        int n = content.length();
        for (int i = 0; i < n; i++) {
            char c = content.charAt(i);
            if (inString) {
                current.append(c);
                if (c == '\'') {
                    if (i + 1 < n && content.charAt(i + 1) == '\'') {
                        current.append('\'');
                        i++;
                        continue;
                    }
                    inString = false;
                }
                continue;
            }
            if (c == '\'') {
                inString = true;
                current.append(c);
                continue;
            }
            if (c == ';') {
                parts.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        if (current.length() > 0) {
            parts.add(current.toString());
        }
        return parts.toArray(new String[0]);
    }

    // SEC-101: MySQL conditional comments (e.g. `/` + `*!50000 ALTER ... ` + `*/`) are
    // executed by the server even though they look like comments. Reject any script
    // containing the `/*!` opener outside a string literal.
    private void rejectConditionalComment(String content, Locale locale) {
        boolean inString = false;
        int n = content.length();
        for (int i = 0; i < n; i++) {
            char c = content.charAt(i);
            if (inString) {
                if (c == '\'') {
                    if (i + 1 < n && content.charAt(i + 1) == '\'') {
                        i++;
                        continue;
                    }
                    inString = false;
                }
                continue;
            }
            if (c == '\'') {
                inString = true;
                continue;
            }
            if (c == '-' && i + 1 < n && content.charAt(i + 1) == '-') {
                while (i < n && content.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }
            if (c == '/' && i + 2 < n
                    && content.charAt(i + 1) == '*'
                    && content.charAt(i + 2) == '!') {
                String msg = messageSource.getMessage("impex.error.conditional.comment", null, locale);
                log.warn("ImpEx rejected: conditional comment detected");
                throw new ImpExInvalidScriptException(msg);
            }
            if (c == '/' && i + 1 < n && content.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < n) {
                    if (content.charAt(i) == '*' && content.charAt(i + 1) == '/') {
                        i++;
                        break;
                    }
                    i++;
                }
            }
        }
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
                if (SELECT_PREFIX.matcher(sql).find()) {
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
                String safe = SqlExceptionTranslator.toSafeMessage(e, messageSource, locale);
                String corr = MDC.get("correlationId");
                String finalMsg = (corr != null && !corr.isBlank())
                        ? safe + " (ref: " + corr + ")"
                        : safe;
                results.add(StatementResult.failure(i + 1, preview, finalMsg));
            }
        }
        return results;
    }

    private String checkBlocked(String sql, Locale locale) {
        Matcher blocked = BLOCKED_PREFIX.matcher(sql);
        if (blocked.find()) {
            String keyword = blocked.group(1).toUpperCase(Locale.ROOT);
            return messageSource.getMessage(
                "impex.statement.blocked", new Object[]{ keyword }, locale
            );
        }

        if (!ALLOWED_PREFIX.matcher(sql).find()) {
            return messageSource.getMessage("impex.statement.unknown", null, locale);
        }

        String sensitive = sensitiveTableTarget(sql);
        if (sensitive != null) {
            return messageSource.getMessage(
                "impex.error.sensitive.table", new Object[]{ sensitive }, locale
            );
        }

        return null;
    }

    static String sensitiveTableTarget(String sql) {
        Matcher m = DML_TARGET_TABLE.matcher(sql);
        if (!m.find()) {
            return null;
        }
        String ref = m.group(1).trim();
        String[] parts = ref.split("\\s*\\.\\s*");
        String last = parts[parts.length - 1];
        last = stripSqlIdentifierQuotes(last).toLowerCase(Locale.ROOT);
        return SENSITIVE_TABLES.contains(last) ? last : null;
    }

    static String stripSqlIdentifierQuotes(String id) {
        if (id == null) {
            return "";
        }
        String s = id.trim();
        while (s.length() >= 2
                && ((s.charAt(0) == '`' && s.charAt(s.length() - 1) == '`')
                        || (s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"'))) {
            s = s.substring(1, s.length() - 1).trim();
        }
        return s;
    }
}
