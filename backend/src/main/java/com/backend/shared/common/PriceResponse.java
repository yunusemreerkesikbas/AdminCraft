package com.backend.shared.common;

import com.backend.domain.enums.Currency;
import com.backend.shared.util.PriceFormatterUtil;

import java.math.BigDecimal;

public record PriceResponse(
        String currencyIso,
        String formattedValue,
        String priceType,
        BigDecimal value
) {

    private static final String DEFAULT_PRICE_TYPE = "BUY";

    public static PriceResponse from(BigDecimal value, Currency currency) {
        return from(value, currency, DEFAULT_PRICE_TYPE);
    }

    public static PriceResponse from(BigDecimal value, Currency currency, String priceType) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        if (currency == null) {
            currency = Currency.getDefault();
        }
        if (priceType == null || priceType.isBlank()) {
            priceType = DEFAULT_PRICE_TYPE;
        }

        String formattedValue = PriceFormatterUtil.format(value, currency);

        return new PriceResponse(
                currency.getIsoCode(),
                formattedValue,
                priceType,
                value
        );
    }
}
