package com.backend.application.commerce.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class CartMergeResponseTest {

	@Test
	void constructor_ShouldDefensivelyCopyWarningMessageKeys() {
		List<String> warnings = new ArrayList<>(List.of("commerce.cart.merge.items.skipped"));

		CartMergeResponse response = new CartMergeResponse(CartMergeStatus.PARTIAL, 1, 1, warnings);
		warnings.add("mutated");

		assertThat(response.warningMessageKeys()).containsExactly("commerce.cart.merge.items.skipped");
		assertThatThrownBy(() -> response.warningMessageKeys().add("new"))
				.isInstanceOf(UnsupportedOperationException.class);
	}
}
