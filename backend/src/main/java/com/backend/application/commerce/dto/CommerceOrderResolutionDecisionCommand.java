package com.backend.application.commerce.dto;

public record CommerceOrderResolutionDecisionCommand(
		boolean approve,
		String decisionNote) {
}
