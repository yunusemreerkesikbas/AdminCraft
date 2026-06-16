package com.backend.infrastructure.persistence.commerce;

import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;

import com.backend.domain.commerce.CommercePaymentAttemptStatus;
import com.backend.testutil.BaseServiceTest;

class CommercePaymentAttemptRepositoryImplTest extends BaseServiceTest {

	@Mock private CommercePaymentAttemptJpaRepository jpaRepository;

	private CommercePaymentAttemptRepositoryImpl repository;

	@BeforeEach
	void setUp() {
		repository = new CommercePaymentAttemptRepositoryImpl(jpaRepository);
	}

	@Test
	void findAdminPaymentAttempts_ShouldDelegateToJpaQuery() {
		PageRequest pageable = PageRequest.of(0, 20);

		repository.findAdminPaymentAttempts("jane", CommercePaymentAttemptStatus.FAILED, pageable);

		verify(jpaRepository).findAdminPaymentAttempts("jane", CommercePaymentAttemptStatus.FAILED, pageable);
	}

	@Test
	void countByStatusAndCreatedAtGreaterThanEqual_ShouldDelegateToJpaQuery() {
		LocalDateTime createdAt = LocalDateTime.of(2026, 6, 10, 0, 0);

		repository.countByStatusAndCreatedAtGreaterThanEqual(CommercePaymentAttemptStatus.FAILED, createdAt);

		verify(jpaRepository).countByStatusAndCreatedAtGreaterThanEqual(CommercePaymentAttemptStatus.FAILED, createdAt);
	}
}
