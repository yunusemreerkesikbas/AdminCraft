package com.backend.shared.common;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * SEC-110: classifies Spring {@link DataAccessException} subtypes into stable i18n keys so the
 * client never sees raw JDBC messages (which can leak schema names, column types, and vendor
 * error codes). Raw exception detail should be logged server-side (for example
 * {@code log.error(..., throwable)}); only the category key should reach API responses.
 */
public final class SqlExceptionTranslator {

    private SqlExceptionTranslator() {}

    public static String classify(Throwable ex) {
        if (ex instanceof BadSqlGrammarException) {
            return "impex.error.sql.syntax";
        }
        if (ex instanceof DuplicateKeyException) {
            return "impex.error.sql.duplicate";
        }
        if (ex instanceof DataIntegrityViolationException) {
            return "impex.error.sql.constraint";
        }
        if (ex instanceof DataAccessException) {
            return "impex.error.sql.dataAccess";
        }
        return "impex.error.sql.unknown";
    }

    public static String toSafeMessage(Throwable ex, MessageSource messageSource, Locale locale) {
        return messageSource.getMessage(classify(ex), null, locale);
    }
}
