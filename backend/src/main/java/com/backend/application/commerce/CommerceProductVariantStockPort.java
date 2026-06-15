package com.backend.application.commerce;

import java.util.Map;

public interface CommerceProductVariantStockPort {

	StockDeductionResult deductIfAvailable(Map<String, Integer> variantQuantities);

	record StockDeductionResult(boolean success, String reasonMessageKey) {
	}
}
