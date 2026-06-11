package com.backend.infrastructure.persistence.commerce;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.backend.application.commerce.CommerceProductVariantLookupPort;
import com.backend.domain.entity.Product;
import com.backend.domain.entity.ProductVariant;
import com.backend.domain.enums.ProductStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class CommerceProductVariantLookupAdapter implements CommerceProductVariantLookupPort {

    private final CommerceProductVariantJpaRepository repository;

    @Override
    public Optional<CommerceVariantSnapshot> findByVariantUid(String variantUid) {
        return repository.findByUid(variantUid)
                .map(this::toSnapshot);
    }

    @Override
    public Map<String, CommerceVariantSnapshot> findByVariantUids(Collection<String> variantUids) {
		if (variantUids == null || variantUids.isEmpty()) {
			return Map.of();
		}
		return repository.findByUidIn(variantUids).stream()
				.map(this::toSnapshot)
				.collect(Collectors.toMap(CommerceVariantSnapshot::variantUid, snapshot -> snapshot));
    }

    private CommerceVariantSnapshot toSnapshot(ProductVariant variant) {
        Product product = variant.getProduct();
        return new CommerceVariantSnapshot(
                product.getUid(),
                product.getSku(),
                ProductStatus.PUBLISHED.equals(product.getStatus()),
                Boolean.TRUE.equals(product.getIsVisible()),
                variant.getUid(),
                variant.getSku(),
                Boolean.TRUE.equals(variant.getActive()),
                variant.getPrice(),
                variant.getVatRate(),
                variant.getStockQuantity());
    }
}
