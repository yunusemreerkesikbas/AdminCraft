package com.backend.infrastructure.persistence.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;

import com.backend.domain.commerce.CommerceOrderStatus;
import com.backend.testutil.BaseServiceTest;

class CommerceOrderRepositoryImplTest extends BaseServiceTest {

	@Mock private CommerceOrderJpaRepository jpaRepository;

	private CommerceOrderRepositoryImpl repository;

	@BeforeEach
	void setUp() {
		repository = new CommerceOrderRepositoryImpl(jpaRepository);
	}

	@Test
	void findByCustomerId_ShouldDelegateToCustomerScopedJpaQuery() {
		PageRequest pageable = PageRequest.of(0, 20);

		repository.findByCustomerId(10L, pageable);

		verify(jpaRepository).findByCustomerId(10L, pageable);
	}

	@Test
	void findByCustomerIdAndUid_ShouldDelegateToCustomerScopedJpaQuery() {
		repository.findByCustomerIdAndUid(10L, "order-uid");

		verify(jpaRepository).findByCustomerIdAndUid(10L, "order-uid");
	}

	@Test
	void findAdminOrders_ShouldDelegateToAdminJpaQuery() {
		PageRequest pageable = PageRequest.of(0, 20);

		repository.findAdminOrders("jane", CommerceOrderStatus.PAID, true, pageable);

		verify(jpaRepository).findAdminOrders("jane", CommerceOrderStatus.PAID, true, pageable);
	}

	@Test
	void findAdminByUid_ShouldDelegateToAdminJpaQuery() {
		repository.findAdminByUid("order-uid");

		verify(jpaRepository).findAdminByUid("order-uid");
	}

	@Test
	void countItemsByOrderIds_ShouldReturnEmptyMap_WhenOrderIdsEmpty() {
		assertThat(repository.countItemsByOrderIds(List.of())).isEmpty();
		verifyNoInteractions(jpaRepository);
	}

	@Test
	void countItemsByOrderIds_ShouldMapProjectionToCountByOrderId() {
		when(jpaRepository.countItemsByOrderIds(List.of(1L, 2L)))
				.thenReturn(List.of(count(1L, 2), count(2L, 3)));

		assertThat(repository.countItemsByOrderIds(List.of(1L, 2L)))
				.containsEntry(1L, 2)
				.containsEntry(2L, 3);
	}

	@Test
	void dashboardCounts_ShouldDelegateToJpaQueries() {
		LocalDateTime start = LocalDateTime.of(2026, 6, 15, 0, 0);
		LocalDateTime end = LocalDateTime.of(2026, 6, 16, 0, 0);

		repository.countByCreatedAtBetween(start, end);
		repository.sumTotalByCreatedAtBetween(start, end);
		repository.countByRequiresAttentionTrue();
		repository.findMostRecentCurrencyIso();

		verify(jpaRepository).countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end);
		verify(jpaRepository).sumTotalByCreatedAtBetween(start, end);
		verify(jpaRepository).countByRequiresAttentionTrue();
		verify(jpaRepository).findMostRecentCurrencyIso(PageRequest.of(0, 1));
	}

	private CommerceOrderJpaRepository.OrderItemCount count(Long orderId, long itemCount) {
		return new CommerceOrderJpaRepository.OrderItemCount() {
			@Override
			public Long getOrderId() {
				return orderId;
			}

			@Override
			public long getItemCount() {
				return itemCount;
			}
		};
	}
}
