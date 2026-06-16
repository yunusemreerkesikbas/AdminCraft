package com.backend.application.commerce.dto;

import java.math.BigDecimal;

public record CommerceAdminMetricResponse(
		long orderCount,
		BigDecimal revenue,
		String currencyIso) {
}
