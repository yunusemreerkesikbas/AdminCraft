package com.backend.application.commerce.dto;

public record CommerceLegalAcceptanceCommand(
		String templateUid,
		Integer version,
		Boolean accepted) {
}
