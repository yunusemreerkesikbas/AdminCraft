package com.backend.infrastructure.util;

import com.backend.domain.enums.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public final class PriceFormatterUtil {

    private static final int DECIMAL_PLACES = 2;

    private PriceFormatterUtil() {
    }

    public static String format(BigDecimal value, Currency currency) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        if (currency == null) {
            currency = Currency.getDefault();
        }

        BigDecimal scaledValue = value.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(currency.getDefaultLocale());
        DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);

        String formattedNumber = formatter.format(scaledValue);

        return formattedNumber + " " + currency.getDisplaySymbol();
    }
}
