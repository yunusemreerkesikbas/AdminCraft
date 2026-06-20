package com.backend.infrastructure.persistence.commerce;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.backend.application.commerce.CommerceProductVariantStockPort;
import com.backend.domain.entity.ProductVariant;
import com.backend.domain.enums.ProductStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class CommerceProductVariantStockAdapter implements CommerceProductVariantStockPort {

	private static final String STOCK_ATTENTION_KEY = "commerce.order.attention.stock_not_deducted";

	private final CommerceProductVariantJpaRepository repository;

	@Override
	public StockDeductionResult deductIfAvailable(Map<String, Integer> variantQuantities) {
		if (variantQuantities == null || variantQuantities.isEmpty()) {
			return new StockDeductionResult(false, STOCK_ATTENTION_KEY);
		}
		Map<String, ProductVariant> variants = repository.findByUidInForUpdate(variantQuantities.keySet()).stream()
				.collect(Collectors.toMap(ProductVariant::getUid, Function.identity()));
		boolean canDeduct = variantQuantities.entrySet().stream()
				.allMatch(entry -> canDeduct(variants.get(entry.getKey()), entry.getValue()));
		if (!canDeduct) {
			return new StockDeductionResult(false, STOCK_ATTENTION_KEY);
		}
		variantQuantities.forEach((variantUid, quantity) -> {
			ProductVariant variant = variants.get(variantUid);
			variant.setStockQuantity(Objects.requireNonNullElse(variant.getStockQuantity(), 0) - quantity);
		});
		return new StockDeductionResult(true, null);
	}

	@Override
	public StockAdjustmentResult restore(Map<String, Integer> variantQuantities) {
		if (variantQuantities == null || variantQuantities.isEmpty()) {
			return new StockAdjustmentResult(false, STOCK_ATTENTION_KEY);
		}
		Map<String, ProductVariant> variants = repository.findByUidInForUpdate(variantQuantities.keySet()).stream()
				.collect(Collectors.toMap(ProductVariant::getUid, Function.identity()));
		boolean canRestore = variantQuantities.entrySet().stream()
				.allMatch(entry -> variants.containsKey(entry.getKey())
						&& entry.getValue() != null
						&& entry.getValue() > 0);
		if (!canRestore) {
			return new StockAdjustmentResult(false, STOCK_ATTENTION_KEY);
		}
		variantQuantities.forEach((variantUid, quantity) -> {
			ProductVariant variant = variants.get(variantUid);
			variant.setStockQuantity(Objects.requireNonNullElse(variant.getStockQuantity(), 0) + quantity);
		});
		return new StockAdjustmentResult(true, null);
	}

	private boolean canDeduct(ProductVariant variant, Integer requestedQuantity) {
		if (variant == null || requestedQuantity == null || requestedQuantity <= 0) {
			return false;
		}
		if (!Boolean.TRUE.equals(variant.getActive())
				|| variant.getProduct() == null
				|| !ProductStatus.PUBLISHED.equals(variant.getProduct().getStatus())
				|| !Boolean.TRUE.equals(variant.getProduct().getIsVisible())) {
			return false;
		}
		return Objects.requireNonNullElse(variant.getStockQuantity(), 0) >= requestedQuantity;
	}
}
