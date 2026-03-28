package com.backend.domain.enums;

import java.util.List;
import java.util.Locale;
import java.util.LinkedHashSet;

public enum ModuleCode {
	CORE("core", "Core"),
	PAGEBUILDER("pagebuilder", "Page Builder"),
	MEDIA("media", "Media Library"),
	COMPONENT_LIBRARY("component_library", "Component Library"),
	PRODUCT_CATALOG("product", "Product Catalog"),
	MAIL_MARKETING("mail_marketing", "Mail Marketing");

	private static final List<ModuleCode> PROVISIONING_SELECTABLE_MODULES = List.of(
			CORE,
			PRODUCT_CATALOG,
			MAIL_MARKETING);

	private static final List<ModuleCode> CORE_EXECUTION_MODULES = List.of(
			MEDIA,
			COMPONENT_LIBRARY,
			PAGEBUILDER);

	private final String code;
	private final String name;

	ModuleCode(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static String normalize(String code) {
		return code == null ? "" : code.trim().toLowerCase(Locale.ROOT);
	}

	public static ModuleCode fromCode(String code) {
		String normalized = normalize(code);
		for (ModuleCode module : values()) {
			if (module.code.equals(normalized)) {
				return module;
			}
		}
		throw new IllegalArgumentException("Unsupported module code: " + code);
	}

	public static boolean isValidCode(String code) {
		try {
			fromCode(code);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public static boolean isProvisioningSelectableCode(String code) {
		String normalized = normalize(code);
		return PROVISIONING_SELECTABLE_MODULES.stream()
				.anyMatch(module -> module.code.equals(normalized));
	}

	public static boolean isCoreExecutionCode(String code) {
		String normalized = normalize(code);
		return CORE_EXECUTION_MODULES.stream()
				.anyMatch(module -> module.code.equals(normalized));
	}

	public static boolean isCoreCoveredCode(String code) {
		String normalized = normalize(code);
		return CORE.code.equals(normalized) || isCoreExecutionCode(normalized);
	}

	public static List<String> provisioningSelectableCodes() {
		return PROVISIONING_SELECTABLE_MODULES.stream()
				.map(ModuleCode::getCode)
				.toList();
	}

	public static List<String> coreExecutionCodes() {
		return CORE_EXECUTION_MODULES.stream()
				.map(ModuleCode::getCode)
				.toList();
	}

	public static List<String> resolveExecutionCodes(List<String> moduleCodes) {
		if (moduleCodes == null || moduleCodes.isEmpty()) {
			return List.of();
		}

		LinkedHashSet<String> resolved = new LinkedHashSet<>();
		for (String rawCode : moduleCodes) {
			String normalized = normalize(rawCode);
			if (normalized.isBlank()) {
				continue;
			}
			if (!isValidCode(normalized)) {
				throw new IllegalArgumentException("Unsupported module code: " + rawCode);
			}
			if (CORE.code.equals(normalized)) {
				resolved.add(CORE.code);
				resolved.addAll(coreExecutionCodes());
			} else {
				resolved.add(normalized);
			}
		}
		return List.copyOf(resolved);
	}
}
