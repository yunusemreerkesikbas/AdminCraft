package com.backend.application.commerce;

import static com.backend.domain.commerce.CommerceCartLimits.MAX_QUANTITY;
import static com.backend.domain.commerce.CommerceCartLimits.MIN_QUANTITY;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.commerce.CommerceProductVariantLookupPort.CommerceVariantSnapshot;
import com.backend.application.commerce.dto.CartMergeResponse;
import com.backend.application.commerce.dto.CartMergeStatus;
import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartItem;
import com.backend.domain.commerce.CommerceCartStatus;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.repository.CommerceCartRepository;
import com.backend.domain.commerce.repository.CommerceCustomerRepository;
import com.backend.domain.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class CustomerCartBridgeServiceImpl implements CustomerCartBridgeService {

	private static final String SKIPPED_WARNING_KEY = "commerce.cart.merge.items.skipped";

	private final CommerceModuleAccessGuard commerceModuleAccessGuard;
	private final CommerceCartRepository cartRepository;
	private final CommerceCustomerRepository customerRepository;
	private final CommerceProductVariantLookupPort productVariantLookupPort;
	private final CartTokenService cartTokenService;
	private final CartService cartService;

	@Override
	@Transactional
	public CustomerCartBridgeResult mergeOnAuth(CommerceCustomer customer, String sourceCartToken) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		if (sourceCartToken == null || sourceCartToken.isBlank()) {
			return new CustomerCartBridgeResult(null, CartMergeResponse.none());
		}
		Optional<CommerceCart> sourceCandidate = findMergeableSourceCart(sourceCartToken);
		if (sourceCandidate.isEmpty()) {
			return new CustomerCartBridgeResult(null, CartMergeResponse.sourceNotFound());
		}

		CommerceCart source = sourceCandidate.get();
		CommerceCustomer lockedCustomer = lockCustomer(customer);
		CommerceCustomerPrincipal principal = new CommerceCustomerPrincipal(
				lockedCustomer.getId(),
				lockedCustomer.getUid(),
				lockedCustomer.getEmail(),
				null);
		Optional<CommerceCart> targetCandidate = cartRepository.findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(
				lockedCustomer.getId(),
				CommerceCartStatus.ACTIVE,
				LocalDateTime.now());
		Map<String, CommerceVariantSnapshot> sourceVariants = loadVariants(source);
		if (targetCandidate.isEmpty()) {
			MergeCounts counts = removeUnmergeableSourceItems(source, sourceVariants);
			source.setCustomer(lockedCustomer);
			CommerceCart linked = cartRepository.save(source);
			CartMergeStatus status = counts.skippedItemCount() > 0 ? CartMergeStatus.PARTIAL : CartMergeStatus.LINKED;
			CartMergeResponse merge = new CartMergeResponse(
					status,
					linked.getItems().size(),
					counts.skippedItemCount(),
					List.copyOf(counts.warningMessageKeys()));
			return new CustomerCartBridgeResult(cartService.getCart(null, principal), merge);
		}

		MergeCounts counts = mergeIntoTarget(source, targetCandidate.get(), sourceVariants);
		source.getItems().clear();
		source.setStatus(CommerceCartStatus.CLEARED);
		cartRepository.save(targetCandidate.get());
		cartRepository.save(source);
		CartMergeStatus status = counts.skippedItemCount() > 0 ? CartMergeStatus.PARTIAL : CartMergeStatus.MERGED;
		CartMergeResponse merge = new CartMergeResponse(
				status,
				counts.mergedItemCount(),
				counts.skippedItemCount(),
				List.copyOf(counts.warningMessageKeys()));
		return new CustomerCartBridgeResult(cartService.getCart(null, principal), merge);
	}

	private Optional<CommerceCart> findMergeableSourceCart(String sourceCartToken) {
		String tokenHash = cartTokenService.hashToken(sourceCartToken);
		return cartRepository.findByTokenHashAndStatusForUpdate(tokenHash, CommerceCartStatus.ACTIVE)
				.filter(cart -> cart.getCustomer() == null)
				.filter(cart -> cart.getExpiresAt() != null && cart.getExpiresAt().isAfter(LocalDateTime.now()));
	}

	private MergeCounts removeUnmergeableSourceItems(CommerceCart source, Map<String, CommerceVariantSnapshot> sourceVariants) {
		int merged = 0;
		int skipped = 0;
		Set<String> warningKeys = new LinkedHashSet<>();
		Iterator<CommerceCartItem> iterator = source.getItems().iterator();
		while (iterator.hasNext()) {
			CommerceCartItem sourceItem = iterator.next();
			CommerceVariantSnapshot variant = sourceVariants.get(sourceItem.getVariantUid());
			if (variant == null || !canMerge(variant, safeQuantity(sourceItem))) {
				iterator.remove();
				skipped++;
				warningKeys.add(SKIPPED_WARNING_KEY);
				continue;
			}
			merged++;
		}
		return new MergeCounts(merged, skipped, warningKeys);
	}

	private MergeCounts mergeIntoTarget(
			CommerceCart source,
			CommerceCart target,
			Map<String, CommerceVariantSnapshot> sourceVariants) {
		var targetItemsByVariantUid = target.getItems().stream()
				.filter(item -> item.getVariantUid() != null)
				.collect(Collectors.toMap(CommerceCartItem::getVariantUid, Function.identity(), (left, right) -> left));
		int merged = 0;
		int skipped = 0;
		Set<String> warningKeys = new LinkedHashSet<>();
		for (CommerceCartItem sourceItem : source.getItems()) {
			CommerceVariantSnapshot variant = sourceVariants.get(sourceItem.getVariantUid());
			CommerceCartItem targetItem = targetItemsByVariantUid.get(sourceItem.getVariantUid());
			int finalQuantity = targetItem == null
					? safeQuantity(sourceItem)
					: safeQuantity(targetItem) + safeQuantity(sourceItem);
			if (variant == null || !canMerge(variant, finalQuantity)) {
				skipped++;
				warningKeys.add(SKIPPED_WARNING_KEY);
				continue;
			}
			if (targetItem == null) {
				CommerceCartItem copied = copyItem(sourceItem);
				target.addItem(copied);
				targetItemsByVariantUid.put(copied.getVariantUid(), copied);
			} else {
				targetItem.setQuantity(finalQuantity);
			}
			merged++;
		}
		return new MergeCounts(merged, skipped, warningKeys);
	}

	private Map<String, CommerceVariantSnapshot> loadVariants(CommerceCart source) {
		Set<String> variantUids = source.getItems().stream()
				.map(CommerceCartItem::getVariantUid)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		if (variantUids.isEmpty()) {
			return Map.of();
		}
		return Optional.ofNullable(productVariantLookupPort.findByVariantUids(variantUids))
				.orElse(Map.of());
	}

	private CommerceCustomer lockCustomer(CommerceCustomer customer) {
		return customerRepository.findByIdForUpdate(customer.getId())
				.orElseThrow(() -> new EntityNotFoundException("commerce.customer.not.found"));
	}

	private boolean canMerge(CommerceVariantSnapshot variant, int quantity) {
		if (!variant.sellable() || quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
			return false;
		}
		int stockQuantity = Objects.requireNonNullElse(variant.stockQuantity(), 0);
		return stockQuantity >= quantity;
	}

	private int safeQuantity(CommerceCartItem item) {
		return Objects.requireNonNullElse(item.getQuantity(), 0);
	}

	private CommerceCartItem copyItem(CommerceCartItem sourceItem) {
		CommerceCartItem copied = new CommerceCartItem();
		copied.setProductUid(sourceItem.getProductUid());
		copied.setProductSku(sourceItem.getProductSku());
		copied.setVariantUid(sourceItem.getVariantUid());
		copied.setVariantSku(sourceItem.getVariantSku());
		copied.setQuantity(sourceItem.getQuantity());
		copied.setUnitGrossPrice(sourceItem.getUnitGrossPrice());
		copied.setVatRate(sourceItem.getVatRate());
		return copied;
	}

	private record MergeCounts(int mergedItemCount, int skippedItemCount, Set<String> warningMessageKeys) {
	}
}
