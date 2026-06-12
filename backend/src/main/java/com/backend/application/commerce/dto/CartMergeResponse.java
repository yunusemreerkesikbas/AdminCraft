package com.backend.application.commerce.dto;

import java.util.List;

public record CartMergeResponse(
		CartMergeStatus status,
		int mergedItemCount,
		int skippedItemCount,
		List<String> warningMessageKeys) {

	public CartMergeResponse {
		warningMessageKeys = warningMessageKeys == null ? List.of() : List.copyOf(warningMessageKeys);
	}

	public static CartMergeResponse none() {
		return new CartMergeResponse(CartMergeStatus.NONE, 0, 0, List.of());
	}

	public static CartMergeResponse sourceNotFound() {
		return new CartMergeResponse(CartMergeStatus.SOURCE_NOT_FOUND, 0, 0, List.of("commerce.cart.merge.source.notFound"));
	}
}
