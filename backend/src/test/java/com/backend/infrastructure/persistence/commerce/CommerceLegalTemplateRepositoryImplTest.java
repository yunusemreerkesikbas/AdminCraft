package com.backend.infrastructure.persistence.commerce;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.backend.domain.commerce.CommerceLegalTemplateType;
import com.backend.domain.port.TenantContextPort;
import com.backend.testutil.BaseServiceTest;

class CommerceLegalTemplateRepositoryImplTest extends BaseServiceTest {

	@Mock private CommerceLegalTemplateJpaRepository jpaRepository;
	@Mock private TenantContextPort tenantContext;

	private CommerceLegalTemplateRepositoryImpl repository;

	@BeforeEach
	void setUp() {
		repository = new CommerceLegalTemplateRepositoryImpl(jpaRepository, tenantContext);
		when(tenantContext.getTenantId()).thenReturn("42");
	}

	@Test
	void acquireTemplateVersionLock_ShouldIncludeTenantIdInNamedLockKey() {
		when(jpaRepository.acquireNamedLock("clt_version:42:DISTANCE_SALES_AGREEMENT:TR")).thenReturn(1);

		repository.acquireTemplateVersionLock(CommerceLegalTemplateType.DISTANCE_SALES_AGREEMENT, "TR");

		verify(jpaRepository).acquireNamedLock("clt_version:42:DISTANCE_SALES_AGREEMENT:TR");
	}

	@Test
	void releaseTemplateVersionLock_ShouldThrow_WhenNamedLockIsNotReleased() {
		when(jpaRepository.releaseNamedLock("clt_version:42:DISTANCE_SALES_AGREEMENT:TR")).thenReturn(0);

		assertThatThrownBy(() -> repository.releaseTemplateVersionLock(
				CommerceLegalTemplateType.DISTANCE_SALES_AGREEMENT,
				"TR"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("commerce.legal.template.version.lock.release.failed");
	}
}
