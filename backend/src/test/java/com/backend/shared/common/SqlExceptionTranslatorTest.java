package com.backend.shared.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;

@ExtendWith(MockitoExtension.class)
class SqlExceptionTranslatorTest {

    @Mock
    private MessageSource messageSource;

    @Test
    @DisplayName("BadSqlGrammarException → impex.error.sql.syntax")
    void classify_badSqlGrammar_returnsSyntaxKey() {
        BadSqlGrammarException ex = new BadSqlGrammarException("ImpEx", "SELECT", new SQLException("boom"));
        assertThat(SqlExceptionTranslator.classify(ex)).isEqualTo("impex.error.sql.syntax");
    }

    @Test
    @DisplayName("DuplicateKeyException → impex.error.sql.duplicate (must precede DataIntegrity)")
    void classify_duplicateKey_returnsDuplicateKey() {
        DuplicateKeyException ex = new DuplicateKeyException("dup");
        assertThat(SqlExceptionTranslator.classify(ex)).isEqualTo("impex.error.sql.duplicate");
    }

    @Test
    @DisplayName("DataIntegrityViolationException (non-duplicate) → impex.error.sql.constraint")
    void classify_dataIntegrity_returnsConstraint() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("fk");
        assertThat(SqlExceptionTranslator.classify(ex)).isEqualTo("impex.error.sql.constraint");
    }

    @Test
    @DisplayName("Generic DataAccessException → impex.error.sql.dataAccess")
    void classify_genericDae_returnsDataAccess() {
        CannotAcquireLockException ex = new CannotAcquireLockException("lock");
        assertThat(SqlExceptionTranslator.classify(ex)).isEqualTo("impex.error.sql.dataAccess");
    }

    @Test
    @DisplayName("Non-DataAccessException → impex.error.sql.unknown")
    void classify_nonDae_returnsUnknown() {
        RuntimeException ex = new RuntimeException("oops");
        assertThat(SqlExceptionTranslator.classify(ex)).isEqualTo("impex.error.sql.unknown");
    }

    @Test
    @DisplayName("toSafeMessage delegates to MessageSource with classified key")
    void toSafeMessage_resolvesKeyViaMessageSource() {
        when(messageSource.getMessage(eq("impex.error.sql.syntax"), any(), any()))
                .thenReturn("syntax error");

        BadSqlGrammarException ex = new BadSqlGrammarException("ImpEx", "SELECT", new SQLException("boom"));
        String result = SqlExceptionTranslator.toSafeMessage(ex, messageSource, Locale.ENGLISH);

        assertThat(result).isEqualTo("syntax error");
    }
}
