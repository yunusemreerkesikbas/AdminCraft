package com.backend.application.commerce.dto;

public record CommerceAdminDashboardResponse(
		CommerceAdminMetricResponse today,
		CommerceAdminMetricResponse lastSevenDays,
		long attentionOrderCount,
		long failedPaymentAttemptCount,
		long failedNotificationCount,
		String currencyIso) {
}
