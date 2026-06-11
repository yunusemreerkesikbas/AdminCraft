package com.backend.application.commerce.dto;

import java.math.BigDecimal;

public record CartTotalsResponse(
        String currencyIso,
        Integer itemCount,
        BigDecimal subtotal,
        BigDecimal vatTotal,
        BigDecimal total) {
}
