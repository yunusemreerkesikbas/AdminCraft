package com.backend.application.commerce;

import java.math.BigDecimal;
import java.util.Optional;

public interface CommerceProductVariantLookupPort {

    Optional<CommerceVariantSnapshot> findByVariantUid(String variantUid);

    record CommerceVariantSnapshot(
            String productUid,
            String productSku,
            boolean productPublished,
            boolean productVisible,
            String variantUid,
            String variantSku,
            boolean variantActive,
            BigDecimal price,
            BigDecimal vatRate,
            Integer stockQuantity) {
        public boolean sellable() {
            return productPublished && productVisible && variantActive;
        }
    }
}
