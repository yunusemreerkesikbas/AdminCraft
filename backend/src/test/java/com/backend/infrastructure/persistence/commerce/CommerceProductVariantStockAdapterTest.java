package com.backend.infrastructure.persistence.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.backend.domain.entity.Product;
import com.backend.domain.entity.ProductVariant;
import com.backend.domain.enums.ProductStatus;
import com.backend.testutil.BaseServiceTest;

class CommerceProductVariantStockAdapterTest extends BaseServiceTest {

	@Mock private CommerceProductVariantJpaRepository repository;

	private CommerceProductVariantStockAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new CommerceProductVariantStockAdapter(repository);
	}

	@Test
	void deductIfAvailable_ShouldDecrementStock_WhenAllVariantsHaveEnoughStock() {
		ProductVariant variant = variant("variant-uid", 5);
		when(repository.findByUidInForUpdate(Map.of("variant-uid", 2).keySet()))
				.thenReturn(List.of(variant));

		var result = adapter.deductIfAvailable(Map.of("variant-uid", 2));

		assertThat(result.success()).isTrue();
		assertThat(variant.getStockQuantity()).isEqualTo(3);
	}

	@Test
	void deductIfAvailable_ShouldNotDecrementAnyStock_WhenOneVariantIsInsufficient() {
		ProductVariant first = variant("variant-1", 5);
		ProductVariant second = variant("variant-2", 1);
		when(repository.findByUidInForUpdate(Map.of("variant-1", 2, "variant-2", 2).keySet()))
				.thenReturn(List.of(first, second));

		var result = adapter.deductIfAvailable(Map.of("variant-1", 2, "variant-2", 2));

		assertThat(result.success()).isFalse();
		assertThat(result.reasonMessageKey()).isEqualTo("commerce.order.attention.stock_not_deducted");
		assertThat(first.getStockQuantity()).isEqualTo(5);
		assertThat(second.getStockQuantity()).isEqualTo(1);
	}

	private ProductVariant variant(String uid, int stockQuantity) {
		Product product = new Product();
		product.setUid("product-" + uid);
		product.setSku("PROD-" + uid);
		product.setStatus(ProductStatus.PUBLISHED);
		product.setIsVisible(true);

		ProductVariant variant = new ProductVariant();
		variant.setUid(uid);
		variant.setSku("SKU-" + uid);
		variant.setProduct(product);
		variant.setActive(true);
		variant.setPrice(BigDecimal.valueOf(100));
		variant.setVatRate(BigDecimal.valueOf(20));
		variant.setStockQuantity(stockQuantity);
		return variant;
	}
}
