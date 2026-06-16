package com.backend.application.commerce.dto;

final class CommerceCustomerNameFormatter {

	private CommerceCustomerNameFormatter() {
	}

	static String format(String firstName, String lastName) {
		StringBuilder name = new StringBuilder();
		appendNamePart(name, firstName);
		appendNamePart(name, lastName);
		return name.isEmpty() ? null : name.toString();
	}

	private static void appendNamePart(StringBuilder name, String value) {
		if (value == null || value.isBlank()) {
			return;
		}
		if (!name.isEmpty()) {
			name.append(' ');
		}
		name.append(value.trim());
	}
}
