package com.backend.domain.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum Currency {

    TRY("TRY", "₺", "TL", "Turkish Lira", new Locale("tr", "TR")),
    USD("USD", "$", "$", "US Dollar", Locale.US),
    EUR("EUR", "€", "€", "Euro", Locale.GERMANY),
    GBP("GBP", "£", "£", "British Pound", Locale.UK),
    JPY("JPY", "¥", "¥", "Japanese Yen", Locale.JAPAN),
    AED("AED", "د.إ", "AED", "UAE Dirham", new Locale("ar", "AE")),
    SAR("SAR", "﷼", "SAR", "Saudi Riyal", new Locale("ar", "SA"));

    private final String isoCode;
    private final String symbol;
    private final String displaySymbol;
    private final String name;
    private final Locale defaultLocale;

    Currency(String isoCode, String symbol, String displaySymbol, String name, Locale defaultLocale) {
        this.isoCode = isoCode;
        this.symbol = symbol;
        this.displaySymbol = displaySymbol;
        this.name = name;
        this.defaultLocale = defaultLocale;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDisplaySymbol() {
        return displaySymbol;
    }

    public String getName() {
        return name;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public String getDisplayName(Language language) {
        return name;
    }

    public static Optional<Currency> fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(currency -> currency.isoCode.equalsIgnoreCase(code.trim()))
                .findFirst();
    }

    public static Currency fromCodeOrDefault(String code, Currency defaultCurrency) {
        return fromCode(code).orElse(defaultCurrency);
    }

    public static Currency fromCodeOrDefault(String code) {
        return fromCodeOrDefault(code, TRY);
    }

    public static Currency getDefault() {
        return TRY;
    }

    public static String[] getAllCodes() {
        return Arrays.stream(values())
                .map(Currency::getIsoCode)
                .toArray(String[]::new);
    }

    public static boolean isSupported(String code) {
        return fromCode(code).isPresent();
    }
}
